package com.future.apix.util;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Objects;

@RunWith(MockitoJUnitRunner.class)
public class LazyObjectWrapperTest {


    @Test
    public void initSuccessTest(){
        LazyObjectWrapper<Integer> objectWrapper = new LazyObjectWrapper<>(new ObjectInitiator<Integer>() {
            @Override
            public Integer initObject() {
                return 0;
            }

            @Override
            public void onInitFailed() {
                throw new RuntimeException();
            }
        });

        Assert.assertEquals(objectWrapper.get(),new Integer(0));
    }

    @Test
    public void initFailedTest(){
        LazyObjectWrapper<Integer> objectWrapper = new LazyObjectWrapper<>(new ObjectInitiator<Integer>() {
            @Override
            public Integer initObject() {
                return null;
            }

            @Override
            public void onInitFailed() {
                throw new RuntimeException("init failed");
            }
        });

        try{
            objectWrapper.get();
            throw new RuntimeException("test failed");
        }
        catch (Exception e){
            Assert.assertEquals(e.getMessage(), "init failed");
        }
    }

    @Test
    public void reInitObjectTest(){
        LazyObjectWrapper<Integer> objectWrapper = new LazyObjectWrapper<>(new ObjectInitiator<Integer>() {
            int counter = 0;
            @Override
            public Integer initObject() {
                return counter++;
            }

            @Override
            public void onInitFailed() {
                throw new RuntimeException("init failed");
            }
        });

        Assert.assertEquals((int)objectWrapper.get(), 0);
        objectWrapper.reInitObject();
        Assert.assertEquals((int)objectWrapper.get(), 1);
    }



}
