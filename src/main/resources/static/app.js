// ===== SHARED UTILITIES AND HELPERS =====

// Status message handling
export function setStatus(el, msg, ok = false) {
  if (!el) return;
  el.innerHTML = msg ? `<span class="${ok ? 'ok' : 'error'}">${msg}</span>` : '';
}

// Authentication fetch with automatic token handling
export async function authFetch(url, options = {}) {
  const token = localStorage.getItem('jwt');
  if (!token) {
    window.location.href = '/login';
    return Promise.reject(new Error('No token'));
  }
  
  options.headers = options.headers || {};
  if (!options.headers['Authorization']) {
    options.headers['Authorization'] = 'Bearer ' + token;
  }
  
  if (options.body && !(options.body instanceof FormData) && !options.headers['Content-Type']) {
    options.headers['Content-Type'] = 'application/json';
  }
  
  const res = await fetch(url, options);
  
  if (res.status === 401 || res.status === 403) {
    localStorage.removeItem('jwt');
    window.location.href = '/login';
    throw new Error('Unauthorized');
  }
  
  return res;
}

// JWT parsing
export function parseJwt(token) {
  try {
    const base64Url = token.split('.')[1];
    const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
    return JSON.parse(decodeURIComponent(atob(base64).split('').map(c =>
      '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2)
    ).join('')));
  } catch {
    return {};
  }
}

// Role-based access control
export function requireRole(required) {
  const token = localStorage.getItem('jwt');
  if (!token) { 
    window.location.href = '/login'; 
    return false; 
  }
  
  const decoded = parseJwt(token);
  const rolesRaw = decoded.roles || decoded.authorities || [];
  const roles = Array.isArray(rolesRaw) ? rolesRaw : String(rolesRaw || '').split(',');
  
  if (required && !roles.includes(required)) {
    window.location.href = '/dashboard';
    return false;
  }
  
  return true;
}

// Logout functionality
export function logout() {
  localStorage.removeItem('jwt');
  window.location.href = '/login';
}

// HTML escaping
export function escapeHtml(s) {
  const div = document.createElement('div');
  div.textContent = s;
  return div.innerHTML;
}

// Safe JSON parsing
export async function safeJson(res) { 
  try { 
    return await res.json(); 
  } catch { 
    return null; 
  } 
}

// ===== FORM VALIDATION UTILITIES =====

export function validatePassword(password, confirmPassword = null) {
  const errors = [];
  if (!password) {
    errors.push('Password is required');
  }
  if (confirmPassword !== null && password !== confirmPassword) {
    errors.push('Passwords do not match');
  }
  return { isValid: errors.length === 0, errors };
}

// Email validation
export function validateEmail(email) {
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  return {
    isValid: emailRegex.test(email),
    error: email ? 'Please enter a valid email address' : 'Email is required'
  };
}

// Required field validation
export function validateRequired(value, fieldName = 'Field') {
  const trimmed = String(value).trim();
  return {
    isValid: !!trimmed,
    error: `${fieldName} is required`
  };
}

// Form validation helper
export function validateForm(formData, rules) {
  const errors = {};
  let isValid = true;
  
  for (const [field, rule] of Object.entries(rules)) {
    const value = formData[field];
    
    if (rule.required) {
      const requiredValidation = validateRequired(value, rule.label || field);
      if (!requiredValidation.isValid) {
        errors[field] = requiredValidation.error;
        isValid = false;
      }
    }
    
    if (rule.email && value) {
      const emailValidation = validateEmail(value);
      if (!emailValidation.isValid) {
        errors[field] = emailValidation.error;
        isValid = false;
      }
    }
    
    if (rule.minLength && value && value.length < rule.minLength) {
      errors[field] = `${rule.label || field} must be at least ${rule.minLength} characters`;
      isValid = false;
    }
    
    if (rule.password && confirmPassword) {
      const passwordValidation = validatePassword(value, confirmPassword);
      if (!passwordValidation.isValid) {
        errors[field] = passwordValidation.errors[0];
        isValid = false;
      }
    }
  }
  
  return {
    isValid,
    errors
  };
}

// ===== UI COMPONENT UTILITIES =====

// Create and show modal
export function createModal(title, content, options = {}) {
  const modal = document.createElement('div');
  modal.className = 'modal';
  modal.innerHTML = `
    <div class="modal-content">
      <div class="modal-header">
        <h3>${escapeHtml(title)}</h3>
        <button class="modal-close">&times;</button>
      </div>
      <div class="modal-body">
        ${content}
      </div>
      ${options.footer ? `
        <div class="modal-footer">
          ${options.footer}
        </div>
      ` : ''}
    </div>
  `;
  
  document.body.appendChild(modal);
  modal.classList.add('show');
  
  // Close functionality
  const closeBtn = modal.querySelector('.modal-close');
  closeBtn.onclick = () => {
    modal.remove();
  };
  
  // Close on outside click
  modal.onclick = (e) => {
    if (e.target === modal) {
      modal.remove();
    }
  };
  
  return modal;
}

// Show confirmation dialog
export function showConfirm(message, onConfirm, onCancel) {
  const modal = createModal('Confirm', `
    <p>${escapeHtml(message)}</p>
  `, {
    footer: `
      <button class="btn btn-secondary">Cancel</button>
      <button class="btn btn-primary">Confirm</button>
    `
  });
  
  const confirmBtn = modal.querySelector('.btn-primary');
  const cancelBtn = modal.querySelector('.btn-secondary');
  
  confirmBtn.onclick = () => {
    modal.remove();
    onConfirm();
  };
  
  cancelBtn.onclick = () => {
    modal.remove();
    onCancel && onCancel();
  };
  
  return modal;
}

// Loading state management
export function setLoading(element, isLoading) {
  if (isLoading) {
    element.classList.add('loading');
    element.disabled = true;
  } else {
    element.classList.remove('loading');
    element.disabled = false;
  }
}

// ===== API CALL UTILITIES =====

// Generic API call wrapper
export async function apiCall(url, options = {}) {
  try {
    const response = await authFetch(url, options);
    
    if (!response.ok) {
     // const error = await safeJson(response);
      // throw new Error(error?.error || error?.message || `HTTP ${response.status}`);

      const errorJson = await safeJson(response);
      const e = new Error(errorJson?.error || errorJson?.message || `HTTP ${response.status}`);
      e.status = response.status;
      e.details = errorJson?.details || null;  // <-- pass structured field errors through
      throw e;
    }
    // Handle 204 and empty responses safely
    if (response.status === 204) return null;
    const text = await response.text();
    return text ? JSON.parse(text) : null;
  } catch (error) {
        console.error('API call failed:', error);
    throw error;
  }
}

// User management utilities
export const userApi = {
  // Get current user
  async getCurrentUser() {
    return apiCall('/rest/members/me');
  },
  
  // Get all users (admin only)
  async getAllUsers(page = 0, size = 10) {
    return apiCall(`/rest/members/all?page=${page}&size=${size}`);
  },
  
  // Search users
  async searchUsers(query, field = 'email', page = 0, size = 10) {
    const params = new URLSearchParams({ field, page, size });
    if (field === 'email') params.set('email', query);
    else params.set('name', query);
    
    return apiCall(`/rest/members/search?${params.toString()}`);
  },
  
  // Get user by ID
  async getUserById(id) {
    return apiCall(`/rest/members/${encodeURIComponent(id)}`);
  },
  
  // Update user
  async updateUser(id, data) {
    return apiCall(`/rest/members/${encodeURIComponent(id)}`, {
      method: 'PUT',
      body: JSON.stringify(data)
    });
  },
  
  // Delete user
  async deleteUser(id) {
    return apiCall(`/rest/members/${encodeURIComponent(id)}`, {
      method: 'DELETE'
    });
  },
  
  // Create user (admin only)
  async createUser(userData) {
    return apiCall('/auth/register-user', {
      method: 'POST',
      body: JSON.stringify(userData)
    });
  }
};

// Authentication utilities
export const authApi = {
  // Login
  async login(email, password) {
    const response = await fetch('/auth/login', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ email, password })
    });
    
    if (!response.ok) {
       const error = await safeJson(response);
       // Force generic message for 400/401
       if (response.status === 400 || response.status === 401) {
         throw new Error('Incorrect email or password. Please check.');
       }
       throw new Error(error?.error || 'Login failed');
    }
    
    const data = await response.json();
    if (!data.token) {
      throw new Error('Login failed: no token returned');
    }
    
    return data;
  },
  
  // Register
  async register(userData) {
    const response = await fetch('/auth/register-user', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(userData)
    });
    
    if (!response.ok) {
            const error = await safeJson(response);
            if (response.status === 403) {
              const e = new Error('Registration is currently disabled by the administrator');
              e.status = 403;
              throw e;
            }
            const e = new Error(error?.error || 'Registration failed');
            e.status = response.status;
            e.details = error?.details || null;
            throw e;
    }
    
    return await response.json();
  }
};

// ===== UTILITY FUNCTIONS =====

// Debounce function
export function debounce(func, wait) {
  let timeout;
  return function executedFunction(...args) {
    const later = () => {
      clearTimeout(timeout);
      func(...args);
    };
    clearTimeout(timeout);
    timeout = setTimeout(later, wait);
  };
}

// Format date
export function formatDate(dateString) {
  const date = new Date(dateString);
  return date.toLocaleDateString('en-US', {
    year: 'numeric',
    month: 'short',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  });
}

// Generate unique ID
export function generateId() {
  return Math.random().toString(36).substr(2, 9);
}

// Deep clone object
export function deepClone(obj) {
  return JSON.parse(JSON.stringify(obj));
}

export function validateUsername(username) {
  const trimmed = String(username || '').trim();
  const usernameRegex = /^[A-Za-z][A-Za-z _]{2,49}$/;

  if (!trimmed) {
    return { isValid: false, error: 'Username is required' };
  }

  return {
    isValid: usernameRegex.test(trimmed),
    error: 'Username must start with a letter, allow letters/spaces/underscores, 3–50 chars'
  };
}

export function validateIndianPhone(phone) {
  const phoneRegex = /^[6-9](?!0{9})\d{9}$/;
  const trimmed = String(phone || '').trim();

  return {
    isValid: phoneRegex.test(trimmed),
    error: trimmed ? 'Invalid Indian phone number. Start 6–9, 10 digits, not all zeros' : 'Phone is required'
  };
}

const THEME_KEY = 'theme';

export function applyTheme(theme) {
  const t = theme === 'dark' ? 'dark' : 'light';
  document.documentElement.setAttribute('data-theme', t);
  localStorage.setItem(THEME_KEY, t);
  // Update any toggle button text if present
  const btn = document.getElementById('themeToggle');
  if (btn) btn.textContent = t === 'dark' ? 'Light' : 'Dark';
}

export function initTheme() {
  const saved = localStorage.getItem(THEME_KEY);
  const prefersDark = window.matchMedia && window.matchMedia('(prefers-color-scheme: dark)').matches;
  applyTheme(saved || (prefersDark ? 'dark' : 'light'));
}

export function attachThemeToggle(buttonElOrId = 'themeToggle') {
  const el = typeof buttonElOrId === 'string' ? document.getElementById(buttonElOrId) : buttonElOrId;
  if (!el) return;
  el.addEventListener('click', () => {
    const current = document.documentElement.getAttribute('data-theme') || 'light';
    applyTheme(current === 'dark' ? 'light' : 'dark');
  });
}

