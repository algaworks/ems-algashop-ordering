package com.algaworks.algashop.ordering.application.utility;

public interface Mapper {
    <D> D convert(Object source, Class<D> destinationType);
}
