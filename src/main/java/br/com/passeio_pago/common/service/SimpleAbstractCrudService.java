package br.com.passeio_pago.common.service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.jpa.repository.JpaRepository;

import br.com.passeio_pago.common.exception.BadRequestException;
import br.com.passeio_pago.common.exception.ElementNotFoundException;
import br.com.passeio_pago.common.exception.ElementRegistrationException;

public abstract class SimpleAbstractCrudService<DTO, ID, ENTITY> implements SimpleCrudService<DTO, ID> {

	private static final Logger LOGGER = LoggerFactory.getLogger(SimpleAbstractCrudService.class);

	@Override
	public List<DTO> getAll() {
		return getDao().findAll().stream().map(entity -> mapEntityToDto(entity)).collect(Collectors.toList());
	}

	@Override
	public DTO register(DTO dto) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug(dto.toString());
		}
		try {
			ENTITY entity = getDao().save(mapDtoToEntity(dto));
			DTO result = mapEntityToDto(entity);
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug(result.toString());
			}
			return result;
		} catch (DataIntegrityViolationException e) {
			if (LOGGER.isErrorEnabled()) {
				LOGGER.error(e.getMessage(), e);
			}
			throw new ElementRegistrationException(e.getMessage());
		}
	}

	@Override
	public void deleteById(ID id) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug(String.format("id toString() = %s", id.toString()));
		}
		try {
			getDao().deleteById(id);
		} catch (IllegalArgumentException e) {
			if (LOGGER.isErrorEnabled()) {
				LOGGER.error(e.getMessage(), e);
			}
			throw new BadRequestException("id must not be null.");
		} catch (NoSuchElementException e) {
			if (LOGGER.isErrorEnabled()) {
				LOGGER.error(e.getMessage(), e);
			}
			throw new ElementNotFoundException(String.format("No such entity with id %s was found.", id.toString()));
		}
	}

	@Override
	public DTO findByID(ID id) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug(String.format("id toString() = %s", id.toString()));
		}
		try {
			ENTITY entity = getDao().findById(id).get();
			return mapEntityToDto(entity);
		} catch (IllegalArgumentException e) {
			if (LOGGER.isErrorEnabled()) {
				LOGGER.error(e.getMessage(), e);
			}
			throw new BadRequestException("id must not be null.");
		} catch (NoSuchElementException e) {
			if (LOGGER.isErrorEnabled()) {
				LOGGER.error(e.getMessage(), e);
			}
			throw new ElementNotFoundException(String.format("No such entity with id %s was found.", id.toString()));
		}
	}

	protected abstract DTO mapEntityToDto(ENTITY entity);

	protected abstract ENTITY mapDtoToEntity(DTO dto);

	protected abstract JpaRepository<ENTITY, ID> getDao();

}
