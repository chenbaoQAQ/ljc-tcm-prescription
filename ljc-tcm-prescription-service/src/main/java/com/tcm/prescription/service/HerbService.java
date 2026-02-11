package com.tcm.prescription.service;

import com.tcm.prescription.common.ErrorCode;
import com.tcm.prescription.dto.HerbCreateReq;
import com.tcm.prescription.dto.HerbQuery;
import com.tcm.prescription.dto.HerbUpdateReq;
import com.tcm.prescription.entity.Herb;
import com.tcm.prescription.exception.ServiceException;
import com.tcm.prescription.repository.HerbRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.persistence.criteria.Predicate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class HerbService {

    private final HerbRepository herbRepository;

    @Transactional
    public Herb create(HerbCreateReq req) {
        if (herbRepository.existsByNameCn(req.getNameCn())) {
            throw new ServiceException(ErrorCode.CONFLICT.getCode(), "Herb name '" + req.getNameCn() + "' already exists");
        }
        Herb herb = new Herb();
        herb.setNameCn(req.getNameCn());
        herb.setDefaultDoseG(req.getDefaultDoseG());
        herb.setNotes(req.getNotes());
        herb.setStatus(1); // Default active
        return herbRepository.save(herb);
    }

    @Transactional
    public Herb update(Long id, HerbUpdateReq req) {
        Herb herb = getById(id);
        
        if (StringUtils.hasText(req.getNameCn()) && !req.getNameCn().equals(herb.getNameCn())) {
            if (herbRepository.existsByNameCn(req.getNameCn())) {
                throw new ServiceException(ErrorCode.CONFLICT.getCode(), "Herb name '" + req.getNameCn() + "' already exists");
            }
            herb.setNameCn(req.getNameCn());
        }
        
        if (req.getDefaultDoseG() != null) {
            herb.setDefaultDoseG(req.getDefaultDoseG());
        }
        if (req.getNotes() != null) {
            herb.setNotes(req.getNotes());
        }
        if (req.getStatus() != null) {
            herb.setStatus(req.getStatus());
        }
        return herbRepository.save(herb);
    }

    public Herb getById(Long id) {
        return herbRepository.findById(id)
                .orElseThrow(() -> new ServiceException(ErrorCode.NOT_FOUND.getCode(), "Herb not found: " + id));
    }

    public Page<Herb> list(HerbQuery query) {
        Specification<Herb> spec = (root, q, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (StringUtils.hasText(query.getKeyword())) {
                predicates.add(cb.like(root.get("nameCn"), "%" + query.getKeyword() + "%"));
            }
            if (query.getStatus() != null) {
                predicates.add(cb.equal(root.get("status"), query.getStatus()));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        return herbRepository.findAll(spec, PageRequest.of(query.getPage() - 1, query.getSize(), Sort.by(Sort.Direction.DESC, "id")));
    }

    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        if (!herbRepository.existsById(id)) {
            throw new ServiceException(ErrorCode.NOT_FOUND.getCode(), "Herb not found");
        }
        herbRepository.deleteById(id);
    }
}
