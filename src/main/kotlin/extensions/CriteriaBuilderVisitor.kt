package extensions

import com.blazebit.persistence.CriteriaBuilder
import com.blazebit.persistence.WhereBuilder
import com.mysema.query.types.*

class CriteriaBuilderVisitor<C : WhereBuilder<C>>(
        val builder: C
) : Visitor<C, C>{

    override fun visit(expr: Constant<*>?, context: C?): C? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun visit(expr: FactoryExpression<*>?, context: C?): C? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun visit(expr: Operation<*>?, context: C?): C? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun visit(expr: ParamExpression<*>?, context: C?): C? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun visit(expr: Path<*>?, context: C?): C? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun visit(expr: SubQueryExpression<*>?, context: C?): C? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun visit(expr: TemplateExpression<*>?, context: C?): C? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}