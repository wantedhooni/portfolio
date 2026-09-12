package com.revy.common.utils;

import com.revy.common.web.api.search.SearchField;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class SearchFieldUtils {
    private SearchFieldUtils() {
    }

    public static List<SearchField> buildSearchField(String paramQuery) {
        List<SearchField> searchFields = new ArrayList<>();

        if(StringUtils.isBlank(paramQuery)) {
            return searchFields;
        }

        String[] filters = paramQuery.split(",");
        for (String filter : filters) {
            String[] tokens = filter.split(":", 2);
            if (tokens.length != 2 || StringUtils.isBlank(tokens[0]) || StringUtils.isBlank(tokens[1])) {
                continue;
            }
            searchFields.add(new SearchField(tokens[0].trim(), tokens[1].trim()));
        }

        return searchFields;
    }
}
