package org.eclipse.xtend.lib.annotations;

import com.google.common.annotations.Beta;
import com.google.common.base.Objects;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import org.eclipse.xtend.lib.annotations.AccessorType;
import org.eclipse.xtend.lib.annotations.Accessors;
import org.eclipse.xtend.lib.annotations.Data;
import org.eclipse.xtend.lib.annotations.FinalFieldsConstructor;
import org.eclipse.xtend.lib.annotations.FinalFieldsConstructorProcessor;
import org.eclipse.xtend.lib.macro.TransformationContext;
import org.eclipse.xtend.lib.macro.TransformationParticipant;
import org.eclipse.xtend.lib.macro.declaration.AnnotationReference;
import org.eclipse.xtend.lib.macro.declaration.AnnotationTarget;
import org.eclipse.xtend.lib.macro.declaration.EnumerationValueDeclaration;
import org.eclipse.xtend.lib.macro.declaration.FieldDeclaration;
import org.eclipse.xtend.lib.macro.declaration.MethodDeclaration;
import org.eclipse.xtend.lib.macro.declaration.MutableClassDeclaration;
import org.eclipse.xtend.lib.macro.declaration.MutableFieldDeclaration;
import org.eclipse.xtend.lib.macro.declaration.MutableMemberDeclaration;
import org.eclipse.xtend.lib.macro.declaration.MutableMethodDeclaration;
import org.eclipse.xtend.lib.macro.declaration.MutableParameterDeclaration;
import org.eclipse.xtend.lib.macro.declaration.TypeReference;
import org.eclipse.xtend.lib.macro.declaration.Visibility;
import org.eclipse.xtend2.lib.StringConcatenation;
import org.eclipse.xtend2.lib.StringConcatenationClient;
import org.eclipse.xtext.xbase.lib.CollectionLiterals;
import org.eclipse.xtext.xbase.lib.Conversions;
import org.eclipse.xtext.xbase.lib.Extension;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import org.eclipse.xtext.xbase.lib.ListExtensions;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;
import org.eclipse.xtext.xbase.lib.Pure;
import org.eclipse.xtext.xbase.lib.StringExtensions;

/**
 * @since 2.7
 * @noextend
 * @noreference
 */
@Beta
@SuppressWarnings("all")
public class AccessorsProcessor implements TransformationParticipant<MutableMemberDeclaration> {
  /**
   * @since 2.7
   * @noextend
   * @noreference
   */
  @Beta
  public static class Util {
    @Extension
    private TransformationContext context;
    
    public Util(final TransformationContext context) {
      this.context = context;
    }
    
    public Visibility toVisibility(final AccessorType type) {
      Visibility _switchResult = null;
      if (type != null) {
        switch (type) {
          case PUBLIC_GETTER:
            _switchResult = Visibility.PUBLIC;
            break;
          case PROTECTED_GETTER:
            _switchResult = Visibility.PROTECTED;
            break;
          case PACKAGE_GETTER:
            _switchResult = Visibility.DEFAULT;
            break;
          case PRIVATE_GETTER:
            _switchResult = Visibility.PRIVATE;
            break;
          case PUBLIC_SETTER:
            _switchResult = Visibility.PUBLIC;
            break;
          case PROTECTED_SETTER:
            _switchResult = Visibility.PROTECTED;
            break;
          case PACKAGE_SETTER:
            _switchResult = Visibility.DEFAULT;
            break;
          case PRIVATE_SETTER:
            _switchResult = Visibility.PRIVATE;
            break;
          default:
            StringConcatenation _builder = new StringConcatenation();
            _builder.append("Cannot convert ");
            _builder.append(type);
            throw new IllegalArgumentException(_builder.toString());
        }
      } else {
        StringConcatenation _builder = new StringConcatenation();
        _builder.append("Cannot convert ");
        _builder.append(type);
        throw new IllegalArgumentException(_builder.toString());
      }
      return _switchResult;
    }
    
    public boolean hasGetter(final FieldDeclaration it) {
      final Function1<String, Boolean> _function = (String name) -> {
        MethodDeclaration _findDeclaredMethod = it.getDeclaringType().findDeclaredMethod(name);
        return Boolean.valueOf((_findDeclaredMethod != null));
      };
      return IterableExtensions.<String>exists(this.getPossibleGetterNames(it), _function);
    }
    
    public boolean shouldAddGetter(final FieldDeclaration it) {
      return ((!this.hasGetter(it)) && (this.getGetterType(it) != AccessorType.NONE));
    }
    
    public AccessorType getGetterType(final FieldDeclaration it) {
      AnnotationReference _elvis = null;
      AnnotationReference _accessorsAnnotation = this.getAccessorsAnnotation(it);
      if (_accessorsAnnotation != null) {
        _elvis = _accessorsAnnotation;
      } else {
        AnnotationReference _accessorsAnnotation_1 = this.getAccessorsAnnotation(it.getDeclaringType());
        _elvis = _accessorsAnnotation_1;
      }
      final AnnotationReference annotation = _elvis;
      if ((annotation != null)) {
        final Function1<EnumerationValueDeclaration, AccessorType> _function = (EnumerationValueDeclaration it_1) -> {
          return AccessorType.valueOf(it_1.getSimpleName());
        };
        final List<AccessorType> types = ListExtensions.<EnumerationValueDeclaration, AccessorType>map(((List<EnumerationValueDeclaration>)Conversions.doWrapArray(annotation.getEnumArrayValue("value"))), _function);
        AccessorType _elvis_1 = null;
        final Function1<AccessorType, Boolean> _function_1 = (AccessorType it_1) -> {
          return Boolean.valueOf(((it_1.name() != null) && it_1.name().endsWith("GETTER")));
        };
        AccessorType _findFirst = IterableExtensions.<AccessorType>findFirst(types, _function_1);
        if (_findFirst != null) {
          _elvis_1 = _findFirst;
        } else {
          _elvis_1 = AccessorType.NONE;
        }
        return _elvis_1;
      }
      return null;
    }
    
    public AnnotationReference getAccessorsAnnotation(final AnnotationTarget it) {
      return it.findAnnotation(this.context.findTypeGlobally(Accessors.class));
    }
    
    public Object validateGetter(final MutableFieldDeclaration field) {
      return null;
    }
    
    public String getGetterName(final FieldDeclaration it) {
      return IterableExtensions.<String>head(this.getPossibleGetterNames(it));
    }
    
    public List<String> getPossibleGetterNames(final FieldDeclaration it) {
      final ArrayList<String> names = CollectionLiterals.<String>newArrayList();
      if ((((this.isBooleanType(this.orObject(it.getType())) && it.getSimpleName().startsWith("is")) && (it.getSimpleName().length() > 2)) && Character.isUpperCase(it.getSimpleName().charAt(2)))) {
        String _simpleName = it.getSimpleName();
        names.add(_simpleName);
      }
      List<String> _xifexpression = null;
      boolean _isBooleanType = this.isBooleanType(this.orObject(it.getType()));
      if (_isBooleanType) {
        _xifexpression = Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList("is", "get"));
      } else {
        _xifexpression = Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList("get"));
      }
      final Function1<String, String> _function = (String prefix) -> {
        String _firstUpper = StringExtensions.toFirstUpper(it.getSimpleName());
        return (prefix + _firstUpper);
      };
      names.addAll(ListExtensions.<String, String>map(_xifexpression, _function));
      return names;
    }
    
    public boolean isBooleanType(final TypeReference it) {
      return ((!it.isInferred()) && Objects.equal(it, this.context.getPrimitiveBoolean()));
    }
    
    public void addGetter(final MutableFieldDeclaration field, final Visibility visibility) {
      this.validateGetter(field);
      field.markAsRead();
      final Procedure1<MutableMethodDeclaration> _function = (MutableMethodDeclaration it) -> {
        this.context.setPrimarySourceElement(it, this.context.getPrimarySourceElement(field));
        it.addAnnotation(this.context.newAnnotationReference(Pure.class));
        it.setReturnType(this.orObject(field.getType()));
        StringConcatenationClient _client = new StringConcatenationClient() {
          @Override
          protected void appendTo(StringConcatenationClient.TargetStringConcatenation _builder) {
            _builder.append("return ");
            Object _fieldOwner = Util.this.fieldOwner(field);
            _builder.append(_fieldOwner);
            _builder.append(".");
            String _simpleName = field.getSimpleName();
            _builder.append(_simpleName);
            _builder.append(";");
          }
        };
        it.setBody(_client);
        it.setStatic(field.isStatic());
        it.setVisibility(visibility);
      };
      field.getDeclaringType().addMethod(this.getGetterName(field), _function);
    }
    
    public AccessorType getSetterType(final FieldDeclaration it) {
      AnnotationReference _elvis = null;
      AnnotationReference _accessorsAnnotation = this.getAccessorsAnnotation(it);
      if (_accessorsAnnotation != null) {
        _elvis = _accessorsAnnotation;
      } else {
        AnnotationReference _accessorsAnnotation_1 = this.getAccessorsAnnotation(it.getDeclaringType());
        _elvis = _accessorsAnnotation_1;
      }
      final AnnotationReference annotation = _elvis;
      if ((annotation != null)) {
        final Function1<EnumerationValueDeclaration, AccessorType> _function = (EnumerationValueDeclaration it_1) -> {
          return AccessorType.valueOf(it_1.getSimpleName());
        };
        final List<AccessorType> types = ListExtensions.<EnumerationValueDeclaration, AccessorType>map(((List<EnumerationValueDeclaration>)Conversions.doWrapArray(annotation.getEnumArrayValue("value"))), _function);
        AccessorType _elvis_1 = null;
        final Function1<AccessorType, Boolean> _function_1 = (AccessorType it_1) -> {
          return Boolean.valueOf(it_1.name().endsWith("SETTER"));
        };
        AccessorType _findFirst = IterableExtensions.<AccessorType>findFirst(types, _function_1);
        if (_findFirst != null) {
          _elvis_1 = _findFirst;
        } else {
          _elvis_1 = AccessorType.NONE;
        }
        return _elvis_1;
      }
      return null;
    }
    
    private Object fieldOwner(final MutableFieldDeclaration it) {
      Object _xifexpression = null;
      boolean _isStatic = it.isStatic();
      if (_isStatic) {
        _xifexpression = this.context.newTypeReference(it.getDeclaringType());
      } else {
        _xifexpression = "this";
      }
      return _xifexpression;
    }
    
    public boolean hasSetter(final FieldDeclaration it) {
      MethodDeclaration _findDeclaredMethod = it.getDeclaringType().findDeclaredMethod(this.getSetterName(it), this.orObject(it.getType()));
      return (_findDeclaredMethod != null);
    }
    
    public String getSetterName(final FieldDeclaration it) {
      String _firstUpper = StringExtensions.toFirstUpper(it.getSimpleName());
      return ("set" + _firstUpper);
    }
    
    public boolean shouldAddSetter(final FieldDeclaration it) {
      return (((!it.isFinal()) && (!this.hasSetter(it))) && (this.getSetterType(it) != AccessorType.NONE));
    }
    
    public void validateSetter(final MutableFieldDeclaration field) {
      boolean _isFinal = field.isFinal();
      if (_isFinal) {
        this.context.addError(field, "Cannot set a final field");
      }
      if (((field.getType() == null) || field.getType().isInferred())) {
        this.context.addError(field, "Type cannot be inferred.");
        return;
      }
    }
    
    public void addSetter(final MutableFieldDeclaration field, final Visibility visibility) {
      this.validateSetter(field);
      final Procedure1<MutableMethodDeclaration> _function = (MutableMethodDeclaration it) -> {
        this.context.setPrimarySourceElement(it, this.context.getPrimarySourceElement(field));
        it.setReturnType(this.context.getPrimitiveVoid());
        final MutableParameterDeclaration param = it.addParameter(field.getSimpleName(), this.orObject(field.getType()));
        StringConcatenationClient _client = new StringConcatenationClient() {
          @Override
          protected void appendTo(StringConcatenationClient.TargetStringConcatenation _builder) {
            Object _fieldOwner = Util.this.fieldOwner(field);
            _builder.append(_fieldOwner);
            _builder.append(".");
            String _simpleName = field.getSimpleName();
            _builder.append(_simpleName);
            _builder.append(" = ");
            String _simpleName_1 = param.getSimpleName();
            _builder.append(_simpleName_1);
            _builder.append(";");
          }
        };
        it.setBody(_client);
        it.setStatic(field.isStatic());
        it.setVisibility(visibility);
      };
      field.getDeclaringType().addMethod(this.getSetterName(field), _function);
    }
    
    private TypeReference orObject(final TypeReference ref) {
      TypeReference _xifexpression = null;
      if ((ref == null)) {
        _xifexpression = this.context.getObject();
      } else {
        _xifexpression = ref;
      }
      return _xifexpression;
    }
  }
  
  @Override
  public void doTransform(final List<? extends MutableMemberDeclaration> elements, @Extension final TransformationContext context) {
    final Consumer<MutableMemberDeclaration> _function = (MutableMemberDeclaration it) -> {
      this.transform(it, context);
    };
    elements.forEach(_function);
  }
  
  protected void _transform(final MutableFieldDeclaration it, @Extension final TransformationContext context) {
    @Extension
    final AccessorsProcessor.Util util = new AccessorsProcessor.Util(context);
    boolean _shouldAddGetter = util.shouldAddGetter(it);
    if (_shouldAddGetter) {
      util.addGetter(it, util.toVisibility(util.getGetterType(it)));
    }
    boolean _shouldAddSetter = util.shouldAddSetter(it);
    if (_shouldAddSetter) {
      util.addSetter(it, util.toVisibility(util.getSetterType(it)));
    }
  }
  
  protected void _transform(final MutableClassDeclaration it, @Extension final TransformationContext context) {
    AnnotationReference _findAnnotation = it.findAnnotation(context.findTypeGlobally(Data.class));
    boolean _tripleNotEquals = (_findAnnotation != null);
    if (_tripleNotEquals) {
      return;
    }
    @Extension
    final FinalFieldsConstructorProcessor.Util requiredArgsUtil = new FinalFieldsConstructorProcessor.Util(context);
    if ((requiredArgsUtil.needsFinalFieldConstructor(it) || (it.findAnnotation(context.findTypeGlobally(FinalFieldsConstructor.class)) != null))) {
      requiredArgsUtil.addFinalFieldsConstructor(it);
    }
    final Function1<MutableFieldDeclaration, Boolean> _function = (MutableFieldDeclaration it_1) -> {
      return Boolean.valueOf(((!it_1.isStatic()) && context.isThePrimaryGeneratedJavaElement(it_1)));
    };
    final Consumer<MutableFieldDeclaration> _function_1 = (MutableFieldDeclaration it_1) -> {
      this._transform(it_1, context);
    };
    IterableExtensions.filter(it.getDeclaredFields(), _function).forEach(_function_1);
  }
  
  public void transform(final MutableMemberDeclaration it, final TransformationContext context) {
    if (it instanceof MutableClassDeclaration) {
      _transform((MutableClassDeclaration)it, context);
      return;
    } else if (it instanceof MutableFieldDeclaration) {
      _transform((MutableFieldDeclaration)it, context);
      return;
    } else {
      throw new IllegalArgumentException("Unhandled parameter types: " +
        Arrays.<Object>asList(it, context).toString());
    }
  }
}
