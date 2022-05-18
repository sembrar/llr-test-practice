package com.ghsembrar.llrtestpreparation;

import static org.junit.Assert.*;

import org.junit.Test;

public class CONSTANTSTest {

    @Test
    public void assert_allow_debug_switched_off() {
        assertFalse("Don't forget to switch ALLOW_DEBUG in release", CONSTANTS.ALLOW_DEBUG);
    }

}
