package it.pagopa.selfcare.user.utils;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.Map;
import java.util.stream.Collectors;

@RequiredArgsConstructor(access = AccessLevel.NONE)
public class QueryUtils {

    public static Map<String, Object> cleanParameterMap(Map<String, Object> parameters) {
        return parameters.entrySet().stream().filter(entry -> entry.getValue() != null )
                .collect( Collectors.toMap( Map.Entry::getKey, Map.Entry::getValue ) );
    }
    public static String buildQuery(Map<String, Object> parameters) {
        return parameters.entrySet().stream()
                .map( entry -> {
                    if(entry.getValue() instanceof ArrayList<?>){
                        return entry.getKey() + "in :" + entry.getKey();
                    }
                    return entry.getKey() + "=:" + entry.getKey();
                })
                .collect( Collectors.joining(" and ") );
    }

}
