package com.kl.parkLine.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.kl.parkLine.entity.Invoice;

@Repository
public interface IInvoiceDao extends JpaRepository<Invoice, Integer>
{
}