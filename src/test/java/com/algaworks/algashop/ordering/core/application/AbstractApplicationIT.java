package com.algaworks.algashop.ordering.core.application;

import com.algaworks.algashop.ordering.core.application.security.SecurityCheckApplicationService;
import com.algaworks.algashop.ordering.core.domain.model.customer.CustomerTestDataBuilder;
import com.algaworks.algashop.ordering.utils.MockJwtDecoderConfig;
import com.algaworks.algashop.ordering.utils.TestcontainerPostgreSQLConfig;
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
public abstract class AbstractApplicationIT {

//	@MockitoBean
//	protected SecurityCheckApplicationService securityCheckApplicationService;

	@BeforeEach
	public void preSetup() {
//		Mockito.when(securityCheckApplicationService.isCustomer()).thenReturn(true);
//		Mockito.when(securityCheckApplicationService.getAuthenticatedUserId())
//				.thenReturn(CustomerTestDataBuilder.DEFAULT_CUSTOMER_ID.value());
	}

}
