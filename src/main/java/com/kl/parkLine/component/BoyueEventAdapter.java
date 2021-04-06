package com.kl.parkLine.component;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.google.common.primitives.Bytes;
import com.kl.parkLine.boyue.BoyueEvent;
import com.kl.parkLine.boyue.BoyueResp;
import com.kl.parkLine.boyue.SerialData;
import com.kl.parkLine.entity.Device;
import com.kl.parkLine.entity.Event;
import com.kl.parkLine.enums.EventType;
import com.kl.parkLine.enums.PlateColor;
import com.kl.parkLine.enums.TriggerType;
import com.kl.parkLine.exception.BusinessException;
import com.kl.parkLine.interfaces.IEventAdapter;
import com.kl.parkLine.json.ContentLines;
import com.kl.parkLine.json.EventResult;
import com.kl.parkLine.service.DeviceService;
import com.kl.parkLine.util.Const;

/**
 * @author chenc
 */
@Component
public class BoyueEventAdapter implements IEventAdapter<BoyueEvent, BoyueResp>
{
    private final static Logger logger = LoggerFactory.getLogger(BoyueEventAdapter.class);
    
    @Autowired
    private DeviceService deviceService;
    
    @Autowired
    private AliYunCmpt aliYunCmpt;
    
    @Autowired
    private CRC16Cmpt crc16Cmpt;
    
    private final  String DEVICE_COMPANY_BY = "博粤";
    
    //车牌颜色, 0：未知、1：蓝色、2：黄色、3：白色、4：黑色、5：绿色
    private final Integer BY_PLATE_COLOR_UNKNOWN = 0;
    private final Integer BY_PLATE_COLOR_BLUE = 1;
    private final Integer BY_PLATE_COLOR_YELLOW = 2;
    private final Integer BY_PLATE_COLOR_WHITE = 3;
    private final Integer BY_PLATE_COLOR_BLACK = 4;
    private final Integer BY_PLATE_COLOR_GREEN = 5;
    
    //触发类型
    private final Integer BY_TRIGGER_TYPE_MANUAL = 2;
    
    //车牌颜色映射表
    private final Map<Integer, PlateColor> mapByPalteColorToEnum = new HashMap<Integer, PlateColor>();
    
    //博粤开闸
    private final String BOYUE_OPEN = "ok";
    private final String BOYUE_NOT_OPEN = "notopen";
    
    private String TIME_STAMP = "`Y-`M-`D `g星期`V `r`H:`N:`S";
    
    private Byte DA = 0x00;
    private Byte VA = 0x64;
    private Byte PN = (byte) 0xFF;
    private Byte CMD_LINES = 0x6E; //单包多行显示，带语音 
    private Byte SAVE_FLAG_TEMP = 0x00; //下载到临时区
    //内容显示控制
    private Byte DM = 0x01; //显示模式
    private Byte DS = 0x00; //显示速度
    private Byte DT = 0x0A; //停留时间 
    private Byte DR = 0x01; //显示次数
    private Byte[] TC_G = {0x00, (byte) 0xFF, 0x00, 0x00}; //文本颜色-绿色
    private Byte TEXT_LINE_END = 0x0D; //文本行结尾
    private Byte TEXT_LAST_LINE_END = 0x00; //最后一行文本行结尾
    private Byte VOICE_START = 0x0A; //语音开始
    private Byte VOICE_END = 0x00; //语音结束
    
    //数据长度位置
    private int DL_POS = 5;
    
    public BoyueEventAdapter()
    {
        //博粤车牌颜色映射表
        mapByPalteColorToEnum.put(BY_PLATE_COLOR_UNKNOWN, PlateColor.unknown);
        mapByPalteColorToEnum.put(BY_PLATE_COLOR_BLUE, PlateColor.blue);
        mapByPalteColorToEnum.put(BY_PLATE_COLOR_YELLOW, PlateColor.yellow);
        mapByPalteColorToEnum.put(BY_PLATE_COLOR_BLACK, PlateColor.black);
        mapByPalteColorToEnum.put(BY_PLATE_COLOR_WHITE, PlateColor.white);
        mapByPalteColorToEnum.put(BY_PLATE_COLOR_GREEN, PlateColor.green);
    } 
    
    @Override
    public Event convert2Event(BoyueEvent boyueEvt) throws BusinessException
    {
        Event retEvent = new Event();
        //厂商
        retEvent.setDeviceCompany(DEVICE_COMPANY_BY);
        //设备序列号
        String serialNo = boyueEvt.getAlarmInfoPlate().getSerialno();
        Device device = deviceService.findOneBySerialNo(serialNo);
        if (null == device)
        {
            throw new BusinessException(String.format("无效的设备序列号: %s", serialNo));
        }
        retEvent.setDeviceSn(serialNo);

        //发生时间
        Long timeStamp = boyueEvt.getAlarmInfoPlate().getResult().getPlateResult().getTimeStamp().getTimeval().getSec()*1000;
        Date happenTime = new Date(timeStamp);
        retEvent.setHappenTime(happenTime);

        //车牌号
        String carNo = boyueEvt.getAlarmInfoPlate().getResult().getPlateResult().getPlateNo().trim().toUpperCase();
        
        //bse64图片处理
        String code = "";
        if (!StringUtils.isEmpty(boyueEvt.getAlarmInfoPlate().getResult().getPlateResult().getImageFile()))
        {
            //code: carNo-in/out-timeStamp.jpg
            Date now = new Date();
            code = String.format("%s-%s-%d.jpg", carNo, device.getUseage(), now.getTime());
            try
            {
                aliYunCmpt.upload(boyueEvt.getAlarmInfoPlate().getResult().getPlateResult().getImageFile(), code);
            }
            catch (IOException e)
            {
                logger.error("图片上传阿里云失败:", e);
            }
        }
        
        //车牌识别Id 
        retEvent.setPlateId(boyueEvt.getAlarmInfoPlate().getResult().getPlateResult().getPlateId());
        
        //事件类型
        switch (device.getUseage())
        {
            case in:
                retEvent.setTimeIn(happenTime);
                retEvent.setType(EventType.in);
                retEvent.setPicUrlIn(code);  
                break;
            case out:
                retEvent.setTimeOut(happenTime);
                retEvent.setType(EventType.complete); //道闸停车将出场当作完成处理
                retEvent.setPicUrlOut(code);  
            default:
                break;
        }
        
        //车牌号码
        retEvent.setPlateNo(carNo);
        
        //车牌颜色
        retEvent.setPlateColor(mapByPalteColorToEnum.get(boyueEvt.getAlarmInfoPlate().getResult().getPlateResult().getColorType()));
        
        //是否有效
        retEvent.setEnabled("Y");
        
        //触发类型
        if (boyueEvt.getAlarmInfoPlate().getResult().getPlateResult().getTriggerType().equals(BY_TRIGGER_TYPE_MANUAL))
        {
            retEvent.setTriggerType(TriggerType.manual);
        }
        else
        {
            retEvent.setTriggerType(TriggerType.auto);
        }
        return retEvent;
    }

    @Override
    public BoyueResp convert2EventResp(EventResult eventResult) throws UnsupportedEncodingException
    {
        BoyueResp boyueResp = new BoyueResp();
        SerialData serialData = this.convertContent(eventResult.getContent());
        if (null != serialData)
        {
            List<SerialData> serialDatas = new ArrayList<>();
            serialDatas.add(serialData);
            boyueResp.setSerialData(serialDatas);
            boyueResp.setContent(serialData.getContent());
        }
        
        if (eventResult.getOpen())
        {
            boyueResp.setInfo(BOYUE_OPEN);
        }
        else
        {
            boyueResp.setInfo(BOYUE_NOT_OPEN);
        }
        return boyueResp;
    }

    @Override
    public BoyueResp failedResp(String messge)
    {
        BoyueResp resp = new BoyueResp();
        resp.setInfo(BOYUE_OPEN); //出现异常回复ok开闸
        return resp;
    }
    
    /**
     * 将文字内容转换成一体机命令
     * @param content
     * @return
     * @throws UnsupportedEncodingException 
     */
    private SerialData convertContent(ContentLines content) throws UnsupportedEncodingException
    {
        if (null == content)
        {
            return null;
        }
        //DA + VR + PN[2] + 0x6E + DL + SAVE_FLAG + TEXT_CONTEXT_NUMBER + TEXT_CONTEXT[…]+ VF+ VTL + VT[...] CRC[2] 
        List<Byte> bytes = new ArrayList<Byte>();
        //设备地址,版本号
        bytes.add(DA);
        bytes.add(VA);
        //单包PN
        bytes.add(PN);
        bytes.add(PN);
        
        //CMD,单包多行显示，带语音 
        bytes.add(CMD_LINES);
        
        //DL,1 byte 数据长度
        int len = 0;
        
        //SAVE_FLAG下载到临时区
        bytes.add(SAVE_FLAG_TEMP);
        len++;
        
        //文本行数
        String strContent = "";
        //将内容转换成数组，方便处理
        Byte lineCnt = 0x00;
        List<String> lines = new ArrayList<>();
        if (!StringUtils.isEmpty(content.getLine1()))
        {
            lines.add(content.getLine1());
            lineCnt++;
            strContent = content.getLine1();
        }
        
        if (!StringUtils.isEmpty(content.getLine2()))
        {
            lines.add(content.getLine2());
            lineCnt++;
            strContent += "#" + content.getLine2();
        }
        
        if (!StringUtils.isEmpty(content.getLine3()))
        {
            lines.add(content.getLine3());
            lineCnt++;
            strContent += "#" + content.getLine3();
        }
        
        if (!StringUtils.isEmpty(content.getLine4()))
        {
            lines.add(content.getLine4());
            lineCnt++;
            strContent += "#" + content.getLine4();
        }
        
        //替换时间戳
        SimpleDateFormat sdf = new SimpleDateFormat("yy-MM-dd E HH:mm:ss");
        strContent = strContent.replace(Const.TIME_STAMP, sdf.format(new Date()));
        
        bytes.add(lineCnt);
        len++;
        
        //转换文本内容
        ListIterator<String> iterator = lines.listIterator();
        byte lid = 0x00; //行号，从0开始
        while (iterator.hasNext())
        {
            String line = iterator.next();
            bytes.add(lid++);
            len++;
            
            //显示模式，速度，停留时间，显示次数
            bytes.add(DM);
            len++;
            bytes.add(DS);
            len++;
            bytes.add(DT);
            len++;
            if (null != content.getDr())
            {
                bytes.add(content.getDr());
            }
            else 
            {
                bytes.add(DR);
            }
            len++;
            
            //文本颜色-绿色 
            bytes.addAll(Arrays.asList(TC_G));
            len += TC_G.length;
            
            //处理时间戳
            if (line.equals(Const.TIME_STAMP))
            {
                line = this.TIME_STAMP;
            }
            //文本长度
            byte[] text = line.getBytes("GBK");
            bytes.add((byte) text.length);
            len++;
            
            //文本内容
            for (byte b : text)
            {
                bytes.add(b);
                len++;
            }
            
            if (iterator.hasNext())
            {
                //文本行结尾
                bytes.add(TEXT_LINE_END);
            }
            else
            {
                //最后文本行结尾
                bytes.add(TEXT_LAST_LINE_END);
            }
            len++;
        }
        
        //语音行
        if (!StringUtils.isEmpty(content.getVoice()))
        {
            bytes.add(VOICE_START);
            len++;
            byte[] text = content.getVoice().getBytes("GBK");
            bytes.add((byte) text.length);
            len++;
            //文本内容
            for (byte b : text)
            {
                bytes.add(b);
                len++;
            }
            bytes.add(VOICE_END);
            len++;
        }
        
        //添加DL
        bytes.add(DL_POS, (byte)(len&0xFF));
        
        //CRC16校验
        bytes.addAll(crc16Cmpt.calcCrc16(bytes));
        
        SerialData serialData = SerialData.builder().serialChannel(0)
                .dataLen(bytes.size())
                .content(strContent)
                .data(new String(Base64.encodeBase64(Bytes.toArray(bytes))))
                .build();
        
        return serialData;
    }
}
