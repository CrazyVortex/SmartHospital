"use strict";

const loginFeedback = document.querySelector("[data-login-feedback]");
if (loginFeedback) {
    const loginErrors = {
        required: "Kullanıcı adı/e-posta ve şifre zorunludur.",
        invalid: "Kullanıcı adı/e-posta veya şifre hatalı.",
        unavailable: "Giriş şu anda gerçekleştirilemiyor. Lütfen daha sonra tekrar deneyin.",
        session: "Oturumunuz sona erdi. Lütfen yeniden giriş yapın.",
        forbidden: "Bu sayfaya erişim yetkiniz yok."
    };
    const errorCode = new URLSearchParams(window.location.search).get("error");
    if (errorCode && Object.prototype.hasOwnProperty.call(loginErrors, errorCode)) {
        loginFeedback.textContent = loginErrors[errorCode];
        loginFeedback.classList.remove("d-none");
    }
}

const passwordToggle = document.querySelector("[data-password-toggle]");
if (passwordToggle) {
    const passwordInput = document.querySelector("#password");
    passwordToggle.addEventListener("click", () => {
        const showPassword = passwordInput.type === "password";
        passwordInput.type = showPassword ? "text" : "password";
        passwordToggle.textContent = showPassword ? "Gizle" : "Göster";
        passwordToggle.setAttribute("aria-pressed", String(showPassword));
    });
}

let getAvailableDemoTimes = function () {
    return [];
};

document.querySelectorAll("form[data-demo-form]").forEach((form) => {
    const feedback = form.querySelector("[data-form-feedback]");

    form.addEventListener("submit", (event) => {
        event.preventDefault();
        form.classList.add("was-validated");

        if (!form.checkValidity()) {
            if (feedback) {
                feedback.textContent = "Lütfen zorunlu alanları kontrol edin.";
                feedback.classList.remove("d-none");
            }

            form.querySelector(":invalid")?.focus();
            return;
        }

        if (feedback) {
            if (form.matches("[data-scheduler-form]")) {
                const clinicId = document.querySelector("#appointmentClinic").value;
                const doctorId = document.querySelector("#appointmentDoctor").value;
                const date = document.querySelector("#appointmentDate").value;
                const time = document.querySelector("#appointmentTime").value;
                const availableTimes = getAvailableDemoTimes(clinicId, doctorId, date);

                if (!availableTimes.includes(time)) {
                    feedback.textContent = "Seçilen saat bu demo doktor ve tarih için uygun değil.";
                    feedback.classList.remove("d-none");
                    return;
                }

                feedback.textContent = "Demo uygunluk kontrolü başarılı; randevu kaydedilmedi.";
            } else {
                feedback.textContent = form.dataset.successMessage
                    || "Form kontrol edildi. Bu arayüz henüz bir sunucuya bağlı değil.";
            }
            feedback.classList.remove("d-none");
        }
    });

    form.addEventListener("reset", () => {
        window.setTimeout(() => {
            form.classList.remove("was-validated");

            if (feedback) {
                feedback.textContent = "";
                feedback.classList.add("d-none");
            }
        }, 0);
    });
});

const today = new Date();
const localToday = new Date(today.getTime() - today.getTimezoneOffset() * 60000)
    .toISOString()
    .slice(0, 10);

document.querySelectorAll("input[type='date'][data-no-past]").forEach((input) => {
    input.min = localToday;
});

const appointmentClinic = document.querySelector("#appointmentClinic");
const appointmentDoctor = document.querySelector("#appointmentDoctor");
const appointmentDate = document.querySelector("#appointmentDate");
const appointmentTime = document.querySelector("#appointmentTime");
const availableTimesList = document.querySelector("#availableTimes");

if (appointmentClinic && appointmentDoctor && appointmentDate
        && appointmentTime && availableTimesList) {
    const demoTimes = [
        "09:00", "09:30", "10:00", "10:30", "11:00",
        "13:00", "13:30", "14:00", "14:30", "15:00"
    ];
    const demoTomorrow = new Date();
    demoTomorrow.setDate(demoTomorrow.getDate() + 1);
    const demoTomorrowKey = new Date(
        demoTomorrow.getTime() - demoTomorrow.getTimezoneOffset() * 60000
    ).toISOString().slice(0, 10);
    const demoAppointments = [
        { doctorId: "demo-doctor-1", date: demoTomorrowKey, time: "09:30", status: "active" },
        { doctorId: "demo-doctor-1", date: demoTomorrowKey, time: "10:00", status: "cancelled" },
        { doctorId: "demo-doctor-2", date: demoTomorrowKey, time: "09:00", status: "active" },
        { doctorId: "demo-doctor-2", date: demoTomorrowKey, time: "09:30", status: "cancelled" }
    ];

    getAvailableDemoTimes = function (clinicId, doctorId, date) {
        if (!clinicId || !doctorId || !date) {
            return [];
        }

        const doctorOption = Array.from(appointmentDoctor.options)
            .find((option) => option.value === doctorId);
        if (!doctorOption || doctorOption.dataset.clinic !== clinicId) {
            return [];
        }

        return demoTimes.filter((time) => !demoAppointments.some((appointment) =>
            appointment.doctorId === doctorId
            && appointment.date === date
            && appointment.time === time
            && appointment.status !== "cancelled"
        ));
    };

    function showDemoTimes() {
        availableTimesList.replaceChildren();
        const clinicId = appointmentClinic.value;
        const doctorId = appointmentDoctor.value;
        const date = appointmentDate.value;

        if (!clinicId || !doctorId || !date) {
            const message = document.createElement("li");
            message.className = "text-muted-custom";
            message.textContent = "Uygun saatleri görmek için poliklinik, doktor ve tarih seçin.";
            availableTimesList.append(message);
            return;
        }

        const availableTimes = getAvailableDemoTimes(clinicId, doctorId, date);
        if (availableTimes.length === 0) {
            const message = document.createElement("li");
            message.className = "text-muted-custom";
            message.textContent = "Bu seçim için uygun demo saati bulunamadı.";
            availableTimesList.append(message);
            return;
        }

        availableTimes.forEach((time) => {
            const item = document.createElement("li");
            const button = document.createElement("button");
            button.type = "button";
            button.className = "btn btn-sm btn-outline-primary";
            button.textContent = time;
            button.setAttribute("aria-label", time + " saatini seç");
            button.addEventListener("click", () => {
                appointmentTime.value = time;
                appointmentTime.dispatchEvent(new Event("input", { bubbles: true }));
                availableTimesList.querySelectorAll("button").forEach((slotButton) => {
                    slotButton.setAttribute("aria-pressed", String(slotButton === button));
                });
            });
            button.setAttribute("aria-pressed", String(appointmentTime.value === time));
            item.append(button);
            availableTimesList.append(item);
        });
    }

    appointmentClinic.addEventListener("change", () => {
        appointmentDoctor.value = "";
        Array.from(appointmentDoctor.options).forEach((option) => {
            if (option.dataset.clinic) {
                const clinicMatches = option.dataset.clinic === appointmentClinic.value;
                option.hidden = !clinicMatches;
                option.disabled = !clinicMatches;
            }
        });
        appointmentTime.value = "";
        showDemoTimes();
    });

    appointmentDoctor.addEventListener("change", () => {
        appointmentTime.value = "";
        showDemoTimes();
    });
    appointmentDate.addEventListener("change", () => {
        appointmentTime.value = "";
        showDemoTimes();
    });
    appointmentTime.addEventListener("change", showDemoTimes);
    showDemoTimes();
}
