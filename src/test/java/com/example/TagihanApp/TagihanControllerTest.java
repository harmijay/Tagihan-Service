package com.example.TagihanApp;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.HashMap;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest(TagihanController.class)
public class TagihanControllerTest {
    @Autowired
    private MockMvc mockMvc;
    private WebClient tagihanClient;

    Logger logger = LoggerFactory.getLogger(TagihanControllerTest.class);

    @MockBean
    private TagihanService tagihanService;

    @Test
    public void testGetTagihan() throws Exception {
//        Restponse response = new Restponse();
//        response.setStatus("200");
//        response.setMessage("succesful");
//        when(tagihanService.getTagihan(any(Long.class))).thenReturn(response);

        logger.warn("tes getTagihan");
        mockMvc.perform(MockMvcRequestBuilders
                        .get("http://localhost:7004/tagihan")
                        .param("idTagihan", "1"))
                .andDo(print())
                .andExpect(status().is(200));
    }
}
