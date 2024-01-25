package fr.gbredz1.teleinfomqtt.conditions;

import fr.gbredz1.teleinfomqtt.configuration.TeleInfoConfiguration;
import io.micronaut.context.condition.Condition;
import io.micronaut.context.condition.ConditionContext;
import io.micronaut.core.annotation.AnnotationMetadataProvider;
import io.micronaut.core.naming.Named;
import io.micronaut.core.value.ValueResolver;
import io.micronaut.inject.qualifiers.Qualifiers;

import java.util.Optional;

public class TeleinfoEnabledFakerCondition implements Condition {
    @Override
    public boolean matches(ConditionContext context) {
        AnnotationMetadataProvider component = context.getComponent();

        if (component instanceof ValueResolver) {
            Optional<String> optional = ((ValueResolver) component).get(Named.class.getName(), String.class);
            if (optional.isPresent()) {
                String name = optional.get();

                TeleInfoConfiguration conf = context.getBean(TeleInfoConfiguration.class, Qualifiers.byName(name));
                return conf.isEnabled() && conf.getFaker() != null;
            }
        }

        return true;
    }
}
