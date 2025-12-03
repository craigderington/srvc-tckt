document.addEventListener('DOMContentLoaded', function() {
    console.log('Service Ticket System - Initialized');

    // Auto-hide alerts after 5 seconds
    const alerts = document.querySelectorAll('.alert');
    alerts.forEach(alert => {
        setTimeout(() => {
            alert.style.opacity = '0';
            alert.style.transition = 'opacity 0.5s ease';
            setTimeout(() => {
                alert.remove();
            }, 500);
        }, 5000);
    });

    // Confirm actions for destructive operations
    const archiveButtons = document.querySelectorAll('form[action*="/archive"] button[type="submit"]');
    archiveButtons.forEach(button => {
        button.addEventListener('click', function(e) {
            if (!confirm('Are you sure you want to archive this ticket? This action will remove it from active views.')) {
                e.preventDefault();
            }
        });
    });

    // Form validation enhancements
    const forms = document.querySelectorAll('form');
    forms.forEach(form => {
        form.addEventListener('submit', function(e) {
            const requiredFields = form.querySelectorAll('[required]');
            let isValid = true;

            requiredFields.forEach(field => {
                if (!field.value.trim()) {
                    isValid = false;
                    field.style.borderColor = '#ef4444';
                } else {
                    field.style.borderColor = '';
                }
            });

            if (!isValid) {
                e.preventDefault();
                alert('Please fill in all required fields.');
            }
        });
    });

    // Add loading state to buttons on form submit
    forms.forEach(form => {
        form.addEventListener('submit', function() {
            const submitButton = form.querySelector('button[type="submit"]');
            if (submitButton && !submitButton.dataset.noLoading) {
                submitButton.disabled = true;
                const originalText = submitButton.textContent;
                submitButton.textContent = 'Processing...';
                submitButton.style.opacity = '0.7';

                // Re-enable after timeout in case of failure
                setTimeout(() => {
                    submitButton.disabled = false;
                    submitButton.textContent = originalText;
                    submitButton.style.opacity = '1';
                }, 10000);
            }
        });
    });

    // Highlight priority badges
    const priorityBadges = document.querySelectorAll('.badge-priority');
    priorityBadges.forEach(badge => {
        badge.style.animation = 'none';
        if (badge.classList.contains('badge-urgent')) {
            badge.style.animation = 'pulse 2s infinite';
        }
    });

    // Add keyboard shortcuts
    document.addEventListener('keydown', function(e) {
        // Alt + H = Home/Dashboard
        if (e.altKey && e.key === 'h') {
            e.preventDefault();
            window.location.href = '/';
        }

        // Alt + Q = Queue
        if (e.altKey && e.key === 'q') {
            e.preventDefault();
            window.location.href = '/tickets/queue';
        }

        // Alt + M = My Tickets
        if (e.altKey && e.key === 'm') {
            e.preventDefault();
            window.location.href = '/tickets/my-tickets';
        }

        // Alt + A = All Tickets
        if (e.altKey && e.key === 'a') {
            e.preventDefault();
            window.location.href = '/tickets';
        }

        // Alt + N = New Ticket
        if (e.altKey && e.key === 'n') {
            e.preventDefault();
            window.location.href = '/tickets/new';
        }
    });

    // Add timestamp tooltips
    const timestamps = document.querySelectorAll('[data-timestamp]');
    timestamps.forEach(element => {
        const timestamp = element.dataset.timestamp;
        const date = new Date(timestamp);
        element.title = date.toLocaleString();
    });

    // Enhanced table row hover effects
    const tableRows = document.querySelectorAll('.stats-table tr');
    tableRows.forEach(row => {
        row.addEventListener('mouseenter', function() {
            this.style.transform = 'scale(1.01)';
            this.style.transition = 'transform 0.2s ease';
        });
        row.addEventListener('mouseleave', function() {
            this.style.transform = 'scale(1)';
        });
    });

    // Auto-refresh for queue page (every 30 seconds)
    if (window.location.pathname.includes('/queue') || window.location.pathname === '/') {
        let refreshInterval = setInterval(() => {
            const lastActivity = Date.now() - (window.lastUserActivity || Date.now());

            // Only refresh if user has been inactive for 30+ seconds
            if (lastActivity > 30000) {
                console.log('Auto-refreshing page...');
                location.reload();
            }
        }, 60000); // Check every 60 seconds

        // Track user activity
        ['mousedown', 'keydown', 'scroll', 'touchstart'].forEach(event => {
            document.addEventListener(event, () => {
                window.lastUserActivity = Date.now();
            });
        });
    }

    // Dynamic ticket age calculation
    function updateTicketAges() {
        const ageElements = document.querySelectorAll('[data-created-at]');
        ageElements.forEach(element => {
            const createdAt = new Date(element.dataset.createdAt);
            const now = new Date();
            const diffMinutes = Math.floor((now - createdAt) / 60000);

            let ageText = '';
            if (diffMinutes < 60) {
                ageText = `${diffMinutes} min ago`;
            } else if (diffMinutes < 1440) {
                ageText = `${Math.floor(diffMinutes / 60)} hours ago`;
            } else {
                ageText = `${Math.floor(diffMinutes / 1440)} days ago`;
            }

            element.textContent = ageText;
        });
    }

    // Update ages every minute
    if (document.querySelectorAll('[data-created-at]').length > 0) {
        updateTicketAges();
        setInterval(updateTicketAges, 60000);
    }

    // Add smooth scrolling
    document.querySelectorAll('a[href^="#"]').forEach(anchor => {
        anchor.addEventListener('click', function(e) {
            e.preventDefault();
            const target = document.querySelector(this.getAttribute('href'));
            if (target) {
                target.scrollIntoView({
                    behavior: 'smooth',
                    block: 'start'
                });
            }
        });
    });

    // Add animation for stat cards on page load
    const statCards = document.querySelectorAll('.stat-card');
    statCards.forEach((card, index) => {
        card.style.opacity = '0';
        card.style.transform = 'translateY(20px)';
        setTimeout(() => {
            card.style.transition = 'all 0.5s ease';
            card.style.opacity = '1';
            card.style.transform = 'translateY(0)';
        }, index * 100);
    });

    // Console shortcuts info
    console.log('%c⌨️ Keyboard Shortcuts:', 'font-size: 14px; font-weight: bold; color: #3b82f6;');
    console.log('Alt + H: Dashboard');
    console.log('Alt + Q: Ticket Queue');
    console.log('Alt + M: My Tickets');
    console.log('Alt + A: All Tickets');
    console.log('Alt + N: New Ticket');
});

// Add CSS animation for urgent badges
const style = document.createElement('style');
style.textContent = `
    @keyframes pulse {
        0%, 100% {
            opacity: 1;
            box-shadow: 0 0 0 0 rgba(220, 38, 38, 0.7);
        }
        50% {
            opacity: 0.9;
            box-shadow: 0 0 0 8px rgba(220, 38, 38, 0);
        }
    }
`;
document.head.appendChild(style);
