package com.kl.parkLine.cmpt;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.kl.parkLine.component.Utils;

@SpringBootTest
public class UtilsTest
{
    @Autowired
    private Utils utils;
    
    @Test
    @Transactional
    public void testDecrypt()
    {
        try
        {
            utils.decrypt("ZlQOFeP9LF/GkDj6WA3bOQ==", "SmD24LoycoNXbrwTBPSz/A==", "PTgo9P/0qRTXZ/o3b6pU8TqSElkRJhzeh4IQ2pO1h1kJxMglTvHd/jm68iPnWA0Nww2eU/MmAmwQktQyxoAI/P7VSW/0S9m4Yf17ZULO1mCkhieMXvKu2gPxh5Q/pqHpc4PDVgO9OLPPCnBQ37/amsryMD1DO5tOGR47A1xWaWboIuINV9mv8S2GibhBFupXAUEYomd/Yh3j053k0koibQ==");
        }
        catch (Exception e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    
}
