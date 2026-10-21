document.querySelectorAll(".scenario-button").forEach((button) => {
  button.addEventListener("click", async () => {
    const result = document.getElementById("result");
    button.disabled = true;
    result.className = "alert alert-info";
    result.textContent = "Running scenario…";
    try {
      const response = await fetch(button.dataset.endpoint, { method: "POST", headers: { "X-Requested-With": "XMLHttpRequest" } });
      const data = await response.json();
      if (!response.ok) throw new Error(data.message || "Scenario failed");
      result.className = "alert alert-success";
      result.textContent = data.result;
    } catch (error) {
      result.className = "alert alert-danger";
      result.textContent = error.message;
    } finally {
      button.disabled = false;
    }
  });
});
