package com.project.chatbackend.repositories;

import com.project.chatbackend.models.Message;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class MessageRepositoryQuery implements IMessageRepositoryQuery {
    private final MongoTemplate mongoTemplate;

    @Override
    public Page<Message> findByContentContaining(String roomId, String search, Date startDate, Date endDate, String senderId, Pageable pageable) {
        Query query = new Query();
        query.addCriteria(Criteria.where("room_id").is(roomId));
        if (search != null && !search.isEmpty()) {
            Criteria contentCriteria = new Criteria();
            contentCriteria.orOperator(
                    Criteria.where("content").regex(search.trim(), "i"),
                    Criteria.where("content.filename").regex(search.trim(), "i")
            );
            query.addCriteria(contentCriteria);
        }

        if (startDate != null && endDate != null) {
            query.addCriteria(Criteria.where("send_date").gte(setStartOfDay(startDate)).lte(setEndOfDay(endDate)));

        }

        if (senderId != null && !senderId.isEmpty()) {
            query.addCriteria(Criteria.where("sender_id").is(senderId));
        }

        query.addCriteria(Criteria.where("message_status").nin(Arrays.asList("REVOKE", "ERROR", "SENDING")));

        query.addCriteria(Criteria.where("message_type").nin(Arrays.asList("AUDIO_CALL", "VIDEO_CALL", "SYSTEM")));

        query.with(Sort.by(Sort.Direction.DESC, "send_date"));

        long totalCount = mongoTemplate.count(query, Message.class);

        query.with(pageable);

        List<Message> result = mongoTemplate.find(query, Message.class);

        return new PageImpl<>(result, pageable, totalCount);
    }

    public Date setEndOfDay(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
        return calendar.getTime();
    }

    public Date setStartOfDay(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }
}
