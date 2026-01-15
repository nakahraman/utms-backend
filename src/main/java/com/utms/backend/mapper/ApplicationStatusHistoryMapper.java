package com.utms.backend.mapper;

import com.utms.backend.model.dto.ApplicationStatusHistoryDto;
import com.utms.backend.statusHistory.ApplicationStatusHistory;
import org.springframework.stereotype.Component;

@Component
public class ApplicationStatusHistoryMapper {

    public ApplicationStatusHistoryDto map(ApplicationStatusHistory h) {
        return new ApplicationStatusHistoryDto(
                h.getId(),
                h.getFromStatus(),
                h.getToStatus(),
                h.getChangedBy(),
                h.getChangedAt()
        );
    }
}
