package cn.chuanwise.xiaoming.lexicons.pro.data;

import cn.chuanwise.utility.CheckUtility;
import cn.chuanwise.utility.StringUtility;
import cn.chuanwise.xiaoming.api.interactor.filter.ParameterFilterMatcher;
import cn.chuanwise.xiaoming.lexicons.pro.LexiconsProPlugin;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.beans.Transient;
import java.util.Objects;
import java.util.regex.Pattern;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LexiconMatcher {
    LexiconMatchType matchType = LexiconMatchType.EQUALS;
    String content;

    transient ParameterFilterMatcher parameterFilterMatcher;
    transient Pattern pattern;

    public LexiconMatcher(LexiconMatchType matchType, String content) {
        this.matchType = matchType;
        this.content = content;

        flush();
    }

    public void setContent(String content) {
        this.content = content;
        flush();
    }

    @Transient
    public ParameterFilterMatcher getParameterFilterMatcher() {
        CheckUtility.checkState(matchType == LexiconMatchType.PARAMETER, "can not call the method: getParameterFilterMatcher() " +
                "for a lexicon matcher without matchType equals to \"PARAMETER\"!");
        if (Objects.isNull(parameterFilterMatcher)) {
            parameterFilterMatcher = new ParameterFilterMatcher(content);
        }
        return parameterFilterMatcher;
    }

    @Transient
    public Pattern getPattern() {
        CheckUtility.checkState(matchType == LexiconMatchType.START_MATCH ||
                matchType == LexiconMatchType.END_MATCH ||
                matchType == LexiconMatchType.CONTAIN_MATCH ||
                matchType == LexiconMatchType.MATCH, "can not call the method: getPattern() " +
                "for a lexicon matcher without matchType equals to \"START_MATCH\", \"END_MATCH\", \"CONTAIN_MATCH\" and \"MATCH\"!");
        if (Objects.isNull(pattern)) {
            pattern = Pattern.compile(content);
        }
        return pattern;
    }

    protected void flush() {
        if (matchType == LexiconMatchType.PARAMETER) {
            getParameterFilterMatcher();
        } else if (matchType == LexiconMatchType.START_MATCH ||
                matchType == LexiconMatchType.END_MATCH ||
                matchType == LexiconMatchType.MATCH) {
            getPattern();
        }
    }

    public boolean apply(String input) {
        switch (matchType) {
            case CONTAIN_EQUAL:
                return input.contains(content);

            case START_EQUAL:
                return input.startsWith(content);
            case END_EQUAL:
                return input.endsWith(content);
            case EQUALS:
                return Objects.equals(input, content);
            case EQUALS_IGNORE_CASE:
                return input.equalsIgnoreCase(content);

            case PARAMETER:
                return getParameterFilterMatcher().matches(input);

            case START_MATCH:
                return StringUtility.startMatches(input, pattern);
            case END_MATCH:
                return StringUtility.endMatches(input, pattern);
            case MATCH:
                return StringUtility.endMatches(input, pattern);

            case CONTAIN_MATCH:
                return pattern.matcher(input).find();

            default:
                LexiconsProPlugin.INSTANCE.throwUnsupportedOperationVersion("matcherType: " + matchType);
                return false;
        }
    }

    @Override
    public String toString() {
        return content + "（" + matchType.toChinese() + "）";
    }
}