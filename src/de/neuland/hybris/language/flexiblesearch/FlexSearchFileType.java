package de.neuland.hybris.language.flexiblesearch;


import com.intellij.openapi.fileTypes.LanguageFileType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class FlexSearchFileType extends LanguageFileType
{
    public static final FlexSearchFileType INSTANCE = new FlexSearchFileType();

    private FlexSearchFileType()
    {
        super(FlexSearchLanguage.INSTANCE);
    }

    @NotNull
    @Override
    public String getName()
    {
        return "Flexible Search file";
    }

    @NotNull
    @Override
    public String getDescription()
    {
        return "Flexible Search language file";
    }

    @NotNull
    @Override
    public String getDefaultExtension()
    {
        return "flexsearch";
    }

    @Nullable
    @Override
    public Icon getIcon()
    {
        return null;
    }

}
