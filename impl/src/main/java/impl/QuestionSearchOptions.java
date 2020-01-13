package impl;

import com.google.common.base.Splitter;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static api.Tag.LABEL_PATTERN;

public class QuestionSearchOptions {
    private static final Pattern tagPattern = Pattern.compile("^\\[(" + LABEL_PATTERN + ")]$");
    private       String         contentSearch;
    private       List<String>   tags       = new ArrayList<>();

    public static QuestionSearchOptions from(String searchQuery) {
        QuestionSearchOptions questionSearchOptions = new QuestionSearchOptions();

        StringBuilder searchStringBuilder = new StringBuilder();
        Splitter
            .on(" ")
            .splitToList(searchQuery)
            .forEach(item -> {
                Matcher matcher = tagPattern.matcher(item);
                if(matcher.matches()) {
                    questionSearchOptions.getTags().add(matcher.group(1));
                    return;
                }
                searchStringBuilder.append(item).append(" ");
            });
        questionSearchOptions.setContentSearch(searchStringBuilder.toString().trim());
        return questionSearchOptions;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public String getContentSearch() {
        return contentSearch;
    }

    public void setContentSearch(String contentSearch) {
        this.contentSearch = contentSearch;
    }
}
