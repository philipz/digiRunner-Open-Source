package tpi.dgrv4.gateway.service;

import org.springframework.stereotype.Component;

/**
 * DgrApiLog2RdbQueue的具體實現類
 * 用於被Spring容器初始化並調用@PostConstruct方法
 */
@Component
public class DgrApiLog2RdbQueueImpl extends DgrApiLog2RdbQueue {
    
    @Override
    public void run() {
        // 此方法是抽象類中的抽象方法，需要實現
        // 但在實際使用中，我們使用匿名類實例化，這個方法不會被調用
        // 這裡僅提供一個空實現以滿足繼承要求
    }
} 