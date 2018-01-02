package com.meiyou.jet.logcat;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
    @Test
    public void useAppContext() throws Exception {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getTargetContext();

        assertEquals("com.meiyou.jet.logcat", appContext.getPackageName());
    }

    @Test
    public void testSubList() {
        ArrayList<Model> list = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            list.add(new Model());
        }

        int size = list.size();
        list = new ArrayList<>(list.subList(size - 5, size));
        int sizeNew = list.size();
        
        assertEquals(size,sizeNew);
    }
    
    
    


    class Model {
        String name = "sdfsdf";
    }
}
