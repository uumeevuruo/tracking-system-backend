package uk.gov.hmcts.dev.service;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import uk.gov.hmcts.dev.dto.CaseRequest;
import uk.gov.hmcts.dev.dto.SearchCriteria;
import uk.gov.hmcts.dev.dto.TaskResponseData;
import uk.gov.hmcts.dev.exception.DuplicateException;
import uk.gov.hmcts.dev.mapper.CaseMapper;
import uk.gov.hmcts.dev.model.CaseStatus;
import uk.gov.hmcts.dev.repository.CaseRepository;

import java.util.UUID;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

@Service
@RequiredArgsConstructor
public class CaseService {
    private final CaseRepository caseRepository;
    private final CaseMapper mapper;

    @Transactional
    public TaskResponseData createCase(CaseRequest request){

        if(isNull(request.status())) {
            request = new CaseRequest(
                    null,
                    request.title(),
                    request.description(),
                    CaseStatus.OPEN,
                    request.due()
            );
        }

        if(caseRepository.existsByTitle(request.title())){
            throw new DuplicateException("title", "Title already exists");
        }

        var response = caseRepository.save(mapper.toCase(request));

        return TaskResponseData.builder()
                .task(mapper.toCaseResponse(response))
                .build();
    }

    public TaskResponseData getCase(SearchCriteria keywords){
        var pageable = PageRequest.of(keywords.page(), keywords.limit(), Sort.by(keywords.sortOrder(), keywords.sortBy()));
        var cases = caseRepository.findAll(
                CaseSearchSpecification.withCriteria(keywords),
                pageable);

        return TaskResponseData.builder()
                .tasks(mapper.pageToCasesResponse(cases))
                .totalElement(cases.getTotalElements())
                .totalPage(cases.getTotalPages())
                .build();
    }

    public TaskResponseData getCase(UUID id){
        var response = caseRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Case not found"));

        return TaskResponseData.builder()
                .task(mapper.toCaseResponse(response))
                .build();
    }

    @Transactional
    public TaskResponseData updateCase(CaseRequest request){
        var response = caseRepository.findById(request.id())
                .orElseThrow(() -> new EntityNotFoundException("Case not found"));

        if(nonNull(request.title())){
            response.setTitle(request.title());
        }

        if(nonNull(request.description())){
            response.setDescription(request.description());
        }

        if(nonNull(request.status())){
            response.setStatus(request.status());
        }

        if(nonNull(request.due())){
            response.setDue(request.due());
        }

        return TaskResponseData.builder()
                .task(mapper.toCaseResponse(caseRepository.save(response)))
                .build();
    }

    @Transactional
    public void deleteCase(UUID id){
        var response = caseRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Case not found"));

        response.setDeleted(true);

        caseRepository.save(response);
    }
}
