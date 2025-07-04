import AsyncStorage from '@react-native-async-storage/async-storage';

// Base storage service untuk operasi CRUD yang reliable
class StorageService {
  // Generic method untuk membaca data
  async getData<T>(key: string): Promise<T[]> {
    try {
      const data = await AsyncStorage.getItem(key);
      if (!data) {
        console.log(`No data found for key: ${key}`);
        return [];
      }
      const parsed = JSON.parse(data);
      console.log(`Retrieved ${parsed.length} items for key: ${key}`);
      return parsed;
    } catch (error) {
      console.error(`Error getting data for key ${key}:`, error);
      return [];
    }
  }

  // Generic method untuk menyimpan data
  async saveData<T>(key: string, data: T[]): Promise<boolean> {
    try {
      const jsonString = JSON.stringify(data);
      await AsyncStorage.setItem(key, jsonString);
      console.log(`Saved ${data.length} items for key: ${key}`);
      return true;
    } catch (error) {
      console.error(`Error saving data for key ${key}:`, error);
      return false;
    }
  }

  // Method untuk menghapus semua data
  async clearData(key: string): Promise<boolean> {
    try {
      await AsyncStorage.removeItem(key);
      console.log(`Cleared data for key: ${key}`);
      return true;
    } catch (error) {
      console.error(`Error clearing data for key ${key}:`, error);
      return false;
    }
  }

  // Method untuk mendapatkan semua keys
  async getAllKeys(): Promise<string[]> {
    try {
      return await AsyncStorage.getAllKeys();
    } catch (error) {
      console.error('Error getting all keys:', error);
      return [];
    }
  }

  // Method untuk backup semua data
  async backupAllData(): Promise<{ [key: string]: any }> {
    try {
      const keys = await this.getAllKeys();
      const backup: { [key: string]: any } = {};
      
      for (const key of keys) {
        const data = await AsyncStorage.getItem(key);
        if (data) {
          backup[key] = JSON.parse(data);
        }
      }
      
      return backup;
    } catch (error) {
      console.error('Error creating backup:', error);
      return {};
    }
  }
}

export const storageService = new StorageService();