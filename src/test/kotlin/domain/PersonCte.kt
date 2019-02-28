package domain

import com.blazebit.persistence.CTE
import javax.persistence.*

@CTE
@Entity
class PersonCte(
        val name: String
) : AbstractJpaPersistable<Long>()
