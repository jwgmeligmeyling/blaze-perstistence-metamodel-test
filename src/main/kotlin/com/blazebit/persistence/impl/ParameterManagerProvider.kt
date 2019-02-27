package com.blazebit.persistence.impl

import com.blazebit.persistence.CriteriaBuilder

fun CriteriaBuilder<*>.parameterManager(): ParameterManager {
    return (this as CriteriaBuilderImpl<*>).parameterManager
}