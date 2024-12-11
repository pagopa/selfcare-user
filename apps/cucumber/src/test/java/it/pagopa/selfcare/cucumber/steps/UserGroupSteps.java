package it.pagopa.selfcare.cucumber.steps;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.pagopa.selfcare.cucumber.dao.UserGroupRepository;
import it.pagopa.selfcare.cucumber.model.UserGroupEntity;
import it.pagopa.selfcare.cucumber.model.UserGroupEntityPageable;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class UserGroupSteps {

    protected String userGroupId;
    protected UserGroupEntity userGroupDetails;
    protected UserGroupEntity userGroupEntityResponse;
    protected UserGroupEntity updatedUserGroupEntity;
    protected UserGroupEntityPageable userGroupEntityResponsePage;
    protected int status;
    protected String errorMessage;
    protected String token = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCIsImtpZCI6Imp3dF9hMjo3YTo0NjozYjoyYTo2MDo1Njo0MDo4ODphMDo1ZDphNDpmODowMToxZTozZSJ9.eyJmYW1pbHlfbmFtZSI6IkJhbGJvYSIsImZpc2NhbF9udW1iZXIiOiJCTEJSS1k2N0MzMEg1MDFCIiwibmFtZSI6IlJvY2t5Iiwic3BpZF9sZXZlbCI6Imh0dHBzOi8vd3d3LnNwaWQuZ292Lml0L1NwaWRMMiIsImZyb21fYWEiOmZhbHNlLCJ1aWQiOiI0YmEyODMyZC05YzRjLTQwZjMtOTEyNi1lMWM3MjkwNWVmMTQiLCJsZXZlbCI6IkwyIiwiaWF0IjoxNzMzOTIxMTg1LCJleHAiOjE3MzM5NTM1ODUsImF1ZCI6ImFwaS5kZXYuc2VsZmNhcmUucGFnb3BhLml0IiwiaXNzIjoiU1BJRCIsImp0aSI6Il8yMGM3YmY1OTdlY2UyNzYxOTUyNyJ9.pc65diXLGvMEJI1CmVZqtkJLu7QdPPkZ7B8xjbj8gkUEItShV-L25O5JBtexkTIm4YJM86rv4ORiIEISPY1oP9vv1CN6I1DUDJXWhh5uMuvK2_soKzhP5IlyxVGl6GtmpD38Q1YVr0faB7EABwrOVUVYEkufZF0WgzYdYQFgG_YPv5qNqEj9SReC0nWSPP8gM5Z1ARA3mzLccEvLy253uZ1-IBbNhFwf2m0xc97lVSqov0jxYaoudFLd8CHaP9SztrOdzMOTIriicasF7gzDgnhtjR7I9IDmkUVLrKFjpyjsV18XfH3UeHkvdtTdjr8kIdmkJgeh3u-CK_QZWiDy0g";

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
