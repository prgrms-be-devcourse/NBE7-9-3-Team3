package org.example.backend.domain.trade.repository

import org.example.backend.domain.trade.entity.TradeImage
import org.springframework.data.jpa.repository.JpaRepository

interface TradeImageRepository : JpaRepository<TradeImage, Long>
