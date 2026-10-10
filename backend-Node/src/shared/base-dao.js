import { readDb, writeDb } from "./db.js";
import { NotFoundError } from "./errors.js";

export class BaseDao {
  static file = "";

  async insert(record) {
    const records = await readDb(this.constructor.file);
    records.push(record);
    await writeDb(this.constructor.file, records);
    return record;
  }

  async findById(id) {
    const records = await readDb(this.constructor.file);
    const record = records.find((candidate) => candidate.id === id);
    if (record === undefined) {
      throw new NotFoundError(`${this.constructor.file}: record ${id} not found`);
    }
    return record;
  }

  async findLatest(filters) {
    const records = await readDb(this.constructor.file);
    const record = records.findLast((candidate) => matches(candidate, filters));
    if (record === undefined) {
      throw new NotFoundError(
        `${this.constructor.file}: no record matching ${JSON.stringify(filters)}`,
      );
    }
    return record;
  }

  async findAll(filters) {
    const records = await readDb(this.constructor.file);
    return records.filter((candidate) => matches(candidate, filters));
  }

  async findAllNewestFirst(filters) {
    return (await this.findAll(filters)).reverse();
  }

  async update(id, fields) {
    const records = await readDb(this.constructor.file);
    const record = records.find((candidate) => candidate.id === id);
    if (record === undefined) {
      throw new NotFoundError(`${this.constructor.file}: record ${id} not found`);
    }
    Object.assign(record, fields);
    await writeDb(this.constructor.file, records);
    return record;
  }

  async delete(id) {
    const records = await readDb(this.constructor.file);
    const remaining = records.filter((candidate) => candidate.id !== id);
    if (remaining.length === records.length) {
      throw new NotFoundError(`${this.constructor.file}: record ${id} not found`);
    }
    await writeDb(this.constructor.file, remaining);
  }
}

function matches(record, filters) {
  return Object.entries(filters).every(([key, value]) => record[key] === value);
}
