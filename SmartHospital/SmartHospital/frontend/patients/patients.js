const API_BASE = '../../backend/php/api/patients';

const patientForm = document.getElementById('patientForm');
const formTitle = document.getElementById('formTitle');
const patientIdInput = document.getElementById('patientId');
const tcNoInput = document.getElementById('tcNo');
const firstNameInput = document.getElementById('firstName');
const lastNameInput = document.getElementById('lastName');
const birthDateInput = document.getElementById('birthDate');
const genderInput = document.getElementById('gender');
const phoneInput = document.getElementById('phone');
const emailInput = document.getElementById('email');
const addressInput = document.getElementById('address');
const submitBtn = document.getElementById('submitBtn');
const cancelEditBtn = document.getElementById('cancelEditBtn');
const formFeedback = document.getElementById('formFeedback');

const searchInput = document.getElementById('searchInput');
const genderFilter = document.getElementById('genderFilter');
const refreshBtn = document.getElementById('refreshBtn');
const patientsTableBody = document.getElementById('patientsTableBody');

let cachedPatients = [];

function showFeedback(message, isSuccess) {
  formFeedback.textContent = message;
  formFeedback.className = `feedback ${isSuccess ? 'feedback-success' : 'feedback-error'}`;
}

function clearFeedback() {
  formFeedback.textContent = '';
  formFeedback.className = 'feedback';
}

function formatGender(gender) {
  if (gender === 'female') return 'Kadın';
  if (gender === 'male') return 'Erkek';
  if (gender === 'other') return 'Diğer';
  return '-';
}

function resetForm() {
  patientForm.reset();
  patientIdInput.value = '';
  formTitle.textContent = 'Yeni Hasta Kaydı';
  submitBtn.textContent = 'Kaydet';
  cancelEditBtn.classList.add('hidden');
}

function fillFormForEdit(patient) {
  patientIdInput.value = patient.id;
  tcNoInput.value = patient.tc_no || '';
  firstNameInput.value = patient.first_name || '';
  lastNameInput.value = patient.last_name || '';
  birthDateInput.value = patient.birth_date || '';
  genderInput.value = patient.gender || '';
  phoneInput.value = patient.phone || '';
  emailInput.value = patient.email || '';
  addressInput.value = patient.address || '';

  formTitle.textContent = `Hasta Bilgilerini Güncelle (#${patient.id})`;
  submitBtn.textContent = 'Güncelle';
  cancelEditBtn.classList.remove('hidden');
  clearFeedback();
  window.scrollTo({ top: 0, behavior: 'smooth' });
}

function renderPatients(rows) {
  if (!Array.isArray(rows) || rows.length === 0) {
    patientsTableBody.innerHTML = '<tr><td colspan="8" class="empty-row">Gösterilecek hasta kaydı bulunamadı.</td></tr>';
    return;
  }

  patientsTableBody.innerHTML = '';

  rows.forEach((patient) => {
    const tr = document.createElement('tr');

    const fullName = `${patient.first_name || ''} ${patient.last_name || ''}`.trim();

    tr.innerHTML = `
      <td>${patient.id}</td>
      <td>${patient.tc_no || '-'}</td>
      <td>${fullName || '-'}</td>
      <td>${patient.birth_date || '-'}</td>
      <td>${formatGender(patient.gender)}</td>
      <td>${patient.phone || '-'}</td>
      <td>${patient.email || '-'}</td>
      <td class="actions-cell">
        <button type="button" class="btn btn-sm btn-edit" data-id="${patient.id}">Düzenle</button>
        <button type="button" class="btn btn-sm btn-danger" data-id="${patient.id}">Sil</button>
      </td>
    `;

    tr.querySelector('.btn-edit').addEventListener('click', () => fillFormForEdit(patient));
    tr.querySelector('.btn-danger').addEventListener('click', () => handleDeletePatient(patient.id, fullName));

    patientsTableBody.appendChild(tr);
  });
}

async function fetchPatients() {
  const params = new URLSearchParams();
  const searchVal = searchInput.value.trim();
  const genderVal = genderFilter.value;

  if (searchVal !== '') {
    params.set('search', searchVal);
  }
  if (genderVal !== '') {
    params.set('gender', genderVal);
  }

  const queryString = params.toString();
  const url = `${API_BASE}/index.php${queryString ? `?${queryString}` : ''}`;

  try {
    const response = await fetch(url);
    const result = await response.json();

    if (!response.ok || !result.success) {
      throw new Error(result.message || 'Hasta listesi alınamadı.');
    }

    cachedPatients = result.data || [];
    renderPatients(cachedPatients);
  } catch (error) {
    patientsTableBody.innerHTML = `<tr><td colspan="8" class="empty-row error-text">${error.message}</td></tr>`;
  }
}

async function handleFormSubmit(event) {
  event.preventDefault();
  clearFeedback();

  const isUpdate = Boolean(patientIdInput.value);
  const endpoint = isUpdate ? `${API_BASE}/update.php` : `${API_BASE}/create.php`;

  const payload = {
    tc_no: tcNoInput.value.trim(),
    first_name: firstNameInput.value.trim(),
    last_name: lastNameInput.value.trim(),
    birth_date: birthDateInput.value || null,
    gender: genderInput.value || null,
    phone: phoneInput.value.trim() || null,
    email: emailInput.value.trim() || null,
    address: addressInput.value.trim() || null,
  };

  if (isUpdate) {
    payload.id = Number(patientIdInput.value);
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
      showFeedback(result.message || 'İşlem gerçekleştirilemedi.', false);
      return;
    }

    showFeedback(result.message || 'İşlem başarılı.', true);
    resetForm();
    await fetchPatients();
  } catch (error) {
    showFeedback(`Sunucu bağlantı hatası: ${error.message}`, false);
  }
}

async function handleDeletePatient(id, fullName) {
  const confirmed = window.confirm(`"${fullName || `#${id}`}" hasta kaydını silmek istediğinize emin misiniz?`);
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
      showFeedback(result.message || 'Hasta silinemedi.', false);
      return;
    }

    showFeedback(result.message || 'Hasta kaydı silindi.', true);
    if (Number(patientIdInput.value) === Number(id)) {
      resetForm();
    }
    await fetchPatients();
  } catch (error) {
    showFeedback(`Silme sırasında hata oluştu: ${error.message}`, false);
  }
}

patientForm.addEventListener('submit', handleFormSubmit);
cancelEditBtn.addEventListener('click', () => {
  resetForm();
  clearFeedback();
});
searchInput.addEventListener('input', fetchPatients);
genderFilter.addEventListener('change', fetchPatients);
refreshBtn.addEventListener('click', fetchPatients);

document.addEventListener('DOMContentLoaded', fetchPatients);