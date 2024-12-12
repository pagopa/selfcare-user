package it.pagopa.selfcare.cucumber.steps;

import it.pagopa.selfcare.cucumber.model.UserGroupEntity;
import it.pagopa.selfcare.cucumber.model.UserGroupEntityPageable;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public class UserGroupSteps {

    protected String userGroupId;
    protected UUID userGroupMemberId;
    protected UserGroupEntity userGroupDetails;
    protected UserGroupEntity userGroupEntityResponse;
    protected UserGroupEntity updatedUserGroupEntity;
    protected UserGroupEntityPageable userGroupEntityResponsePage;
    protected Pageable pageable;
    protected String sort;

    protected  UserGroupEntity userGroupEntityFilter;
    protected int status;
    protected String errorMessage;

    protected String token = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCIsImtpZCI6Imp3dF9hMjo3YTo0NjozYjoyYTo2MDo1Njo0MDo4ODphMDo1ZDphNDpmODowMToxZTozZSJ9.eyJmYW1pbHlfbmFtZSI6IkJhbGJvYSIsImZpc2NhbF9udW1iZXIiOiJCTEJSS1k2N0MzMEg1MDFCIiwibmFtZSI6IlJvY2t5Iiwic3BpZF9sZXZlbCI6Imh0dHBzOi8vd3d3LnNwaWQuZ292Lml0L1NwaWRMMiIsImZyb21fYWEiOmZhbHNlLCJ1aWQiOiI0YmEyODMyZC05YzRjLTQwZjMtOTEyNi1lMWM3MjkwNWVmMTQiLCJsZXZlbCI6IkwyIiwiaWF0IjoxNzMzOTk2MzQwLCJleHAiOjE3MzQwMjg3NDAsImF1ZCI6ImFwaS5kZXYuc2VsZmNhcmUucGFnb3BhLml0IiwiaXNzIjoiU1BJRCIsImp0aSI6Il8zZTg4NzkyNzRmODJlMGY5Yzk0MSJ9.myOhEFshQKu0agM9jIg0WakcQF0rNVRSrh9k2k5-LA3SkIUpN_Io6d9dLg9Rb7aTGqU50BxEk0OP4zGDJSUjT6pHfWJz0emGFL9UkVJ-w9Jc7WX2oMuj-E9ej9YuB7pXNYM9bKvg4PpE4ZMvnLVxvh_AuTmDVM9nK7K0uD1vvPWptmIwQSjlJhA68LWVxLMlfMVoCk7ysmtIdAkbD4QkqFsP1mQKCOSH2EQUyz_NQFgYWX70-9boSuwx8kOTOHturG1xAuLCGXqhk6yk_HgJShDkeDI6Q9C73dYBE_nNKeJ-yg10vNfiYV5MPjjFiW4eeqvKw5ATf8LhhJmIrjMDcw";

   /* public Map<String, Object> readDataPopulation() {
        ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory());
        TypeReference<Map<String, Object>> typeReference = new TypeReference<>() {
        };
        Map<String, Object> readValue = new HashMap<>();
        try {
            return objectMapper.readValue(new File("src/test/resources/dataPopulation/data.yaml"), typeReference);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return readValue;
    }*/

}
