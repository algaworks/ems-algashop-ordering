package com.algaworks.algashop.ordering.core.application;

import com.algaworks.algashop.ordering.core.application.security.SecurityCheckApplicationService;
import com.algaworks.algashop.ordering.core.domain.model.customer.CustomerTestDataBuilder;
import com.algaworks.algashop.ordering.utils.MockJwtDecoderConfig;
import com.algaworks.algashop.ordering.utils.TestcontainerPostgreSQLConfig;
import com.algaworks.algashop.ordering.utils.WithMockJwt;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({TestcontainerPostgreSQLConfig.class, MockJwtDecoderConfig.class})
@WithMockJwt
public abstract class AbstractApplicationIT {

}
