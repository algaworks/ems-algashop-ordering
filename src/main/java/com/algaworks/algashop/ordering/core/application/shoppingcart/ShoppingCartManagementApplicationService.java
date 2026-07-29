package com.algaworks.algashop.ordering.core.application.shoppingcart;

import com.algaworks.algashop.ordering.core.domain.model.commons.Quantity;
import com.algaworks.algashop.ordering.core.domain.model.customer.CustomerId;
import com.algaworks.algashop.ordering.core.domain.model.product.Product;
import com.algaworks.algashop.ordering.core.domain.model.product.ProductCatalogService;
import com.algaworks.algashop.ordering.core.domain.model.product.ProductId;
import com.algaworks.algashop.ordering.core.domain.model.product.ProductNotFoundException;
import com.algaworks.algashop.ordering.core.domain.model.shoppingcart.*;
import com.algaworks.algashop.ordering.core.ports.in.shoppingcart.ForManagingShoppingCarts;
import com.algaworks.algashop.ordering.core.ports.in.shoppingcart.ShoppingCartItemInput;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ShoppingCartManagementApplicationService implements ForManagingShoppingCarts {

	private final ShoppingCarts shoppingCarts;
	private final ProductCatalogService productCatalogService;
	private final ShoppingService shoppingService;

	@Transactional
	@Override
	public void addItem(ShoppingCartItemInput input) {
		Objects.requireNonNull(input);
		ShoppingCartId shoppingCartId = new ShoppingCartId(input.getShoppingCartId());
		ProductId productId = new ProductId(input.getProductId());

		ShoppingCart shoppingCart = shoppingCarts.ofId(shoppingCartId)
				.orElseThrow(()-> new ShoppingCartNotFoundException(shoppingCartId.value()));

		Product product = productCatalogService.ofId(productId)
				.orElseThrow(()-> new ProductNotFoundException(productId));

		shoppingCart.addItem(product, new Quantity(input.getQuantity()));

		shoppingCarts.add(shoppingCart);
	}

	@Transactional
	@Override
	public UUID createNew(UUID rawCustomerId) {
		Objects.requireNonNull(rawCustomerId);
		ShoppingCart shoppingCart = shoppingService.startShopping(new CustomerId(rawCustomerId));
		shoppingCarts.add(shoppingCart);
		return shoppingCart.id().value();
	}

	@Transactional
	@Override
	public void removeItem(UUID rawShoppingCartId, UUID rawShoppingCartItemId) {
		Objects.requireNonNull(rawShoppingCartId);
		Objects.requireNonNull(rawShoppingCartItemId);
		ShoppingCartId shoppingCartId = new ShoppingCartId(rawShoppingCartId);
		ShoppingCart shoppingCart = shoppingCarts.ofId(shoppingCartId)
				.orElseThrow(()-> new ShoppingCartNotFoundException(rawShoppingCartId));
		shoppingCart.removeItem(new ShoppingCartItemId(rawShoppingCartItemId));
		shoppingCarts.add(shoppingCart);
	}

	@Transactional
	@Override
	public void empty(UUID rawShoppingCartId) {
		Objects.requireNonNull(rawShoppingCartId);
		ShoppingCartId shoppingCartId = new ShoppingCartId(rawShoppingCartId);
		ShoppingCart shoppingCart = shoppingCarts.ofId(shoppingCartId)
				.orElseThrow(()-> new ShoppingCartNotFoundException(rawShoppingCartId));
		shoppingCart.empty();
		shoppingCarts.add(shoppingCart);
	}

	@Transactional
	@Override
	public void delete(UUID rawShoppingCartId) {
		Objects.requireNonNull(rawShoppingCartId);
		ShoppingCartId shoppingCartId = new ShoppingCartId(rawShoppingCartId);
		ShoppingCart shoppingCart = shoppingCarts.ofId(shoppingCartId)
				.orElseThrow(()-> new ShoppingCartNotFoundException(rawShoppingCartId));
		shoppingCarts.remove(shoppingCart);
	}

	@Override
	public void changeProductAvailability(UUID productId, boolean available) {
		List<ShoppingCart> affectedShoppingCarts = shoppingCarts.findAllContainingItem(new ProductId(productId));

		if (affectedShoppingCarts.isEmpty()) {
			return;
		}

		affectedShoppingCarts.forEach(shoppingCart -> {
			shoppingCart.changeItemAvailability(new ProductId(productId), available);
			shoppingCarts.add(shoppingCart);
		});
	}

	@Override
	public void refreshProductPrice(UUID productId) {
		ProductId domainProductId = new ProductId(productId);
		List<ShoppingCart> affectedShoppingCarts = shoppingCarts.findAllContainingItem(domainProductId);

		if (affectedShoppingCarts.isEmpty()) {
			return;
		}

		Product product = productCatalogService.ofId(domainProductId)
				.orElseThrow(() -> new ProductNotFoundException(domainProductId));

		affectedShoppingCarts.forEach(shoppingCart -> {
			shoppingCart.refreshItem(product);
			shoppingCarts.add(shoppingCart);
		});
	}

}