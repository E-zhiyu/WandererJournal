package com.wanderer.journal.auxiliary.classes.text;

import androidx.annotation.NonNull;

import com.wanderer.journal.auxiliary.enums.KeyStrings;
import com.wanderer.journal.auxiliary.enums.RichTextRegex;
import com.wanderer.journal.auxiliary.interfaces.RichTextRule;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class RoleRefTextRule implements RichTextRule {
    @Override
    public Pattern getPattern() {
        return RichTextRegex.ROLE_REF.getPattern();
    }

    @Override
    public String getDisplayText(@NonNull Matcher matcher) {
        return "@" + matcher.group(1);
    }

    @Override
    public String getKey() {
        return KeyStrings.ROLE_ID.getS();
    }

    @Override
    public String getTextTagData(@NonNull Matcher matcher) {
        return matcher.group(2);
    }
}
