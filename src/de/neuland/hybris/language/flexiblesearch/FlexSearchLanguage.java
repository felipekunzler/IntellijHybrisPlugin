package de.neuland.hybris.language.flexiblesearch;


import com.intellij.lang.Language;

public class FlexSearchLanguage extends Language
{
    public static final FlexSearchLanguage INSTANCE = new FlexSearchLanguage();

    private FlexSearchLanguage()
    {
        super("FlexSearch");
    }
}
