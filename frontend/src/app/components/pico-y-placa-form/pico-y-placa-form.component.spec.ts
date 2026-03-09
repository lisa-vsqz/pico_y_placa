import { ComponentFixture, TestBed } from "@angular/core/testing";
import { ReactiveFormsModule } from "@angular/forms";
import { HttpClientTestingModule } from "@angular/common/http/testing";
import { PicoYPlacaFormComponent } from "./pico-y-placa-form.component";

describe("PicoYPlacaFormComponent", () => {
  let component: PicoYPlacaFormComponent;
  let fixture: ComponentFixture<PicoYPlacaFormComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [
        PicoYPlacaFormComponent,
        ReactiveFormsModule,
        HttpClientTestingModule,
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(PicoYPlacaFormComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it("debería crear el componente", () => {
    expect(component).toBeTruthy();
  });

  it("debería inicializar el formulario con campos vacíos", () => {
    expect(component.form.get("plateNumber")?.value).toBe("");
    expect(component.form.get("dateTime")?.value).toBe("");
  });

  it("debería marcar como inválido si la placa está vacía", () => {
    component.form.get("plateNumber")?.setValue("");
    component.form.get("plateNumber")?.markAsTouched();
    expect(component.form.get("plateNumber")?.hasError("required")).toBeTrue();
  });

  it("debería marcar como inválido si la placa tiene formato incorrecto", () => {
    component.form.get("plateNumber")?.setValue("12345");
    component.form.get("plateNumber")?.markAsTouched();
    expect(component.form.get("plateNumber")?.hasError("pattern")).toBeTrue();
  });

  it("debería aceptar una placa válida con guión", () => {
    component.form.get("plateNumber")?.setValue("PBX-1234");
    expect(component.form.get("plateNumber")?.valid).toBeTrue();
  });

  it("debería aceptar una placa válida sin guión", () => {
    component.form.get("plateNumber")?.setValue("PBX1234");
    expect(component.form.get("plateNumber")?.valid).toBeTrue();
  });

  it("debería aceptar una placa en minúsculas", () => {
    component.form.get("plateNumber")?.setValue("pbx-1234");
    expect(component.form.get("plateNumber")?.valid).toBeTrue();
  });

  it("debería rechazar placas con caracteres especiales", () => {
    component.form.get("plateNumber")?.setValue("<script>");
    expect(component.form.get("plateNumber")?.hasError("pattern")).toBeTrue();
  });

  it("debería marcar la fecha como requerida", () => {
    component.form.get("dateTime")?.setValue("");
    component.form.get("dateTime")?.markAsTouched();
    expect(component.form.get("dateTime")?.hasError("required")).toBeTrue();
  });

  it("debería rechazar una fecha en el pasado", () => {
    component.form.get("dateTime")?.setValue("2020-01-01T08:00");
    expect(component.form.get("dateTime")?.hasError("dateInPast")).toBeTrue();
  });

  it("debería aceptar una fecha futura", () => {
    component.form.get("dateTime")?.setValue("2030-06-15T10:00");
    expect(component.form.get("dateTime")?.valid).toBeTrue();
  });

  it("no debería enviar si el formulario es inválido", () => {
    component.onSubmit();
    expect(component.isLoading).toBeFalse();
  });

  it("debería limpiar el formulario y resultados al resetear", () => {
    component.result = {
      plateNumber: "PBX-1234",
      dateTime: "",
      canDrive: true,
      message: "test",
    };
    component.errorMessage = "error test";
    component.onReset();

    expect(component.result).toBeNull();
    expect(component.errorMessage).toBeNull();
    expect(component.form.get("plateNumber")?.value).toBeFalsy();
  });
});
