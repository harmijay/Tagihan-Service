package com.example.TagihanApp;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class TagihanIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    Logger logger = LoggerFactory.getLogger(TagihanIntegrationTest.class);

    @Test
    public void testGetTagihan() throws Exception {

        logger.error("tes log error");
        mockMvc.perform(MockMvcRequestBuilders
                .get("http://localhost:7004/tagihan/1"))
                .andDo(print())
                .andExpect(status().is(200));
    }
}
