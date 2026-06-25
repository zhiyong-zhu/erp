import { useMemo, useState } from "react";
import { fetchInventoryLocations, fetchInventoryWarehouses } from "../../../api/inventory";
import type { InventoryLocationRecord, InventoryWarehouseRecord } from "../../../types/inventory";

export function useInventoryPositions() {
  const [warehouses, setWarehouses] = useState<InventoryWarehouseRecord[]>([]);
  const [locations, setLocations] = useState<InventoryLocationRecord[]>([]);

  const warehouseOptions = useMemo(
    () => warehouses.map((item) => ({ label: `${item.code} · ${item.name}`, value: item.id })),
    [warehouses]
  );

  const locationOptions = useMemo(
    () => locations.map((item) => ({ label: `${item.warehouseCode}/${item.code} · ${item.name}`, value: item.id })),
    [locations]
  );

  async function loadPositions() {
    const [warehouseData, locationData] = await Promise.all([
      fetchInventoryWarehouses({ pageNum: 1, pageSize: 200, status: 1 }),
      fetchInventoryLocations({ pageNum: 1, pageSize: 500, status: 1 })
    ]);
    setWarehouses(warehouseData.records);
    setLocations(locationData.records);
  }

  function applyWarehouse(values: Record<string, any>, warehouseId?: string, prefix = "") {
    const warehouse = warehouses.find((item) => item.id === warehouseId);
    if (!warehouse) return values;
    return {
      ...values,
      [positionKey(prefix, "warehouseId")]: warehouse.id,
      [positionKey(prefix, "warehouseCode")]: warehouse.code,
      [positionKey(prefix, "warehouseName")]: warehouse.name
    };
  }

  function applyLocation(values: Record<string, any>, locationId?: string, prefix = "") {
    const location = locations.find((item) => item.id === locationId);
    if (!location) return values;
    return {
      ...values,
      [positionKey(prefix, "locationId")]: location.id,
      [positionKey(prefix, "warehouseId")]: location.warehouseId,
      [positionKey(prefix, "warehouseCode")]: location.warehouseCode,
      [positionKey(prefix, "warehouseName")]: location.warehouseName,
      [positionKey(prefix, "locationCode")]: location.code,
      [positionKey(prefix, "locationName")]: location.name
    };
  }

  return {
    warehouses,
    locations,
    warehouseOptions,
    locationOptions,
    loadPositions,
    applyWarehouse,
    applyLocation
  };
}

function positionKey(prefix: string, key: string) {
  if (!prefix) {
    return key;
  }
  return `${prefix}${key.charAt(0).toUpperCase()}${key.slice(1)}`;
}
