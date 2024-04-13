package com.example;

import io.github.rosemoe.sora.widget.schemes.EditorColorScheme;
import io.github.rosemoe.sora.widget.schemes.SchemeVS2019;
import io.github.rosemoe.sora.widget.schemes.SchemeGitHub;
import io.github.rosemoe.sora.widget.schemes.SchemeEclipse;
import io.github.rosemoe.sora.widget.schemes.SchemeDarcula;
import io.github.rosemoe.sora.widget.schemes.SchemeNotepadXX;

public enum ColorSchemes {
    
    AIDE_Light(new AIDEColorSchemes.Light()),
    AIDE_Dark(new AIDEColorSchemes.Dark()),
    VS2019(new SchemeVS2019()),
    Default(new EditorColorScheme()),
    Github(new SchemeGitHub()),
    Eclipse(new SchemeEclipse()),
    Darcula(new SchemeDarcula()),
    NotePadPP(new SchemeNotepadXX());
    
    private EditorColorScheme scheme;
    
    ColorSchemes(EditorColorScheme scheme) {
        this.scheme = scheme;
    }
    
}
