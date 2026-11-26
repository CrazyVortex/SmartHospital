const API_BASE = '../../backend/php/api/appointments';

const appointmentForm = document.getElementById('appointmentForm');
const formTitle = document.getElementById('formTitle');
const appointmentIdInput = document.getElementById('appointmentId');
const patientIdInput = document.getElementById('patientId');
const clinicIdInput = document.getElementById('clinicId');
const doctorIdInput = document.getElementById('doctorId');
const appointmentDateInput = document.getElementById('appointmentDate');
const appointmentTimeInput = document.getElementById('appointmentTime');
const statusInput = document.getElementById('status');
const notesInput = document.getElementById('notes');
const submitBtn = document.getElementById('submitBtn');
const cancelEditBtn = document.getElementById('cancelEditBtn');
const formFeedback = document.getElementById('formFeedback');

const searchInput = document.getElementById('searchInput');
const dateFilter = document.getElementById('dateFilter');
const statusFilter = document.getElementById('statusFilter');
const refreshBtn = document.getElementById('refreshBtn');
const appointmentsTableBody = document.getElementById('appointmentsTableBody');

function showFeedback(message, isSuccess) {
  formFeedback.textContent = message;
  formFeedback.className = `feedback ${isSuccess ? 'feedback-success' : 'feedback-error'}`;
}

function clearFeedback() {
  formFeedback.textContent = '';
  formFeedback.className = 'feedback';
}

function formatStatus(status) {
  if (status === 'scheduled') return 'Planlandı';
  if (status === 'completed') return 'Tamamlandı';
  if (status === 'cancelled') return 'İptal Edildi';
  return status || '-';
}

function resetForm() {
  appointmentForm.reset();
  appointmentIdInput.value = '';
  patientIdInput.disabled = false;
  statusInput.value = 'scheduled';
  formTitle.textContent = 'Yeni Randevu Oluştur';
  submitBtn.textContent = 'Kaydet';
  cancelEditBtn.classList.add('hidden');
}

function fillFormForEdit(appointment) {
  appointmentIdInput.value = appointment.id;
  patientIdInput.value = appointment.patient_id;
  patientIdInput.disabled = true;
  clinicIdInput.value = appointment.clinic_id;
  doctorIdInput.value = appointment.doctor_id;
  appointmentDateInput.value = appointment.appointment_date || '';
  appointmentTimeInput.value = (appointment.appointment_time || '').slice(0, 5);
  statusInput.value = appointment.status || 'scheduled';
  notesInput.value = appointment.notes || '';

  formTitle.textContent = `Randevu Güncelle (#${appointment.id})`;
  submitBtn.textContent = 'Güncelle';
  cancelEditBtn.classList.remove('hidden');
  clearFeedback();
  window.scrollTo({ top: 0, behavior: 'smooth' });
}

function renderAppointments(rows) {
  if (!Array.isArray(rows) || rows.length === 0) {
    appointmentsTableBody.innerHTML = '<tr><td colspan="9" class="empty-row">Gösterilecek randevu kaydı bulunamadı.</td></tr>';
    return;
  }

  appointmentsTableBody.innerHTML = '';

  rows.forEach((item) => {
    const tr = document.createElement('tr');
    const patientFullName = `${item.patient_first_name || ''} ${item.patient_last_name || ''}`.trim();
    const patientDisplay = patientFullName
      ? `${patientFullName} (${item.patient_tc_no || '-'})`
      : `#${item.patient_id}`;
    const clinicDisplay = item.clinic_name || `#${item.clinic_id}`;
    const doctorDisplay = item.doctor_name || `#${item.doctor_id}`;
    const timeDisplay = (item.appointment_time || '').slice(0, 5) || '-';

    tr.innerHTML = `
      <td>${item.id}</td>
      <td>${patientDisplay}</td>
      <td>${clinicDisplay}</td>
      <td>${doctorDisplay}</td>
      <td>${item.appointment_date || '-'}</td>
      <td>${timeDisplay}</td>
      <td>${formatStatus(item.status)}</td>
      <td>${item.notes || '-'}</td>
      <td class="actions-cell">
        <button type="button" class="btn btn-sm btn-edit" data-id="${item.id}">Düzenle</button>
        <button type="button" class="btn btn-sm btn-danger" data-id="${item.id}">Sil</button>
      </td>
    `;

    tr.querySelector('.btn-edit').addEventListener('click', () => fillFormForEdit(item));
    tr.querySelector('.btn-danger').addEventListener('click', () => handleDeleteAppointment(item.id, patientDisplay));

    appointmentsTableBody.appendChild(tr);
  });
}

async function fetchAppointments() {
  const params = new URLSearchParams();
  const searchVal = searchInput.value.trim();
  const dateVal = dateFilter.value;
  const statusVal = statusFilter.value;

  if (searchVal !== '') {
    params.set('search', searchVal);
  }
  if (dateVal !== '') {
    params.set('appointment_date', dateVal);
  }
  if (statusVal !== '') {
    params.set('status', statusVal);
  }

  const queryString = params.toString();
  const url = `${API_BASE}/index.php${queryString ? `?${queryString}` : ''}`;

  try {
    const response = await fetch(url);
    const result = await response.json();

    if (!response.ok || !result.success) {
      throw new Error(result.message || 'Randevu listesi alınamadı.');
    }

    renderAppointments(result.data || []);
  } catch (error) {
    appointmentsTableBody.innerHTML = `<tr><td colspan="9" class="empty-row error-text">${error.message}</td></tr>`;
  }
}

async function handleFormSubmit(event) {
  event.preventDefault();
  clearFeedback();

  const isUpdate = Boolean(appointmentIdInput.value);
  const endpoint = isUpdate ? `${API_BASE}/update.php` : `${API_BASE}/create.php`;

  const payload = {
    patient_id: Number(patientIdInput.value),
    clinic_id: Number(clinicIdInput.value),
    doctor_id: Number(doctorIdInput.value),
    appointment_date: appointmentDateInput.value,
    appointment_time: appointmentTimeInput.value,
    status: statusInput.value,
    notes: notesInput.value.trim() || null,
  };

  if (isUpdate) {
    payload.id = Number(appointmentIdInput.value);
  }

  try {
    const response = await fetch(endpoint, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(payload),
    });

    const result = await response.json();

    if (!response.ok || !result.success) {
      showFeedback(result.message || 'Randevu işlemi gerçekleştirilemedi.', false);
      return;
    }

    showFeedback(result.message || 'Randevu işlemi başarılı.', true);
    resetForm();
    await fetchAppointments();
  } catch (error) {
    showFeedback(`Sunucu bağlantı hatası: ${error.message}`, false);
  }
}

async function handleDeleteAppointment(id, label) {
  const confirmed = window.confirm(`"${label || `#${id}`}" randevu kaydını silmek istediğinize emin misiniz?`);
  if (!confirmed) return;

  try {
    const response = await fetch(`${API_BASE}/delete.php`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({ id: Number(id) }),
    });

    const result = await response.json();

    if (!response.ok || !result.success) {
      showFeedback(result.message || 'Randevu silinemedi.', false);
      return;
    }

    showFeedback(result.message || 'Randevu kaydı silindi.', true);
    if (Number(appointmentIdInput.value) === Number(id)) {
      resetForm();
    }
    await fetchAppointments();
  } catch (error) {
    showFeedback(`Silme sırasında hata oluştu: ${error.message}`, false);
  }
}

appointmentForm.addEventListener('submit', handleFormSubmit);
cancelEditBtn.addEventListener('click', () => {
  resetForm();
  clearFeedback();
});
searchInput.addEventListener('input', fetchAppointments);
dateFilter.addEventListener('change', fetchAppointments);
statusFilter.addEventListener('change', fetchAppointments);
refreshBtn.addEventListener('click', fetchAppointments);

document.addEventListener('DOMContentLoaded', fetchAppointments);