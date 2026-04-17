package com.qtm.dashboard.medicine.mapper;

import com.qtm.commonlib.dto.MedicineDto;
import com.qtm.dashboard.medicine.entity.MedicineEntity;
import org.springframework.stereotype.Component;

/**
 * Mapper tra entity farmaco locale e DTO condiviso verso frontend e altri moduli.
 */
@Component
public class MedicineMapper {

    public void updateEntity(MedicineEntity entity, MedicineDto dto) {
        entity.setCodiceAic(dto.getCodiceAic());
        entity.setCodFarmaco(dto.getCodFarmaco());
        entity.setCodConfezione(dto.getCodConfezione());
        entity.setDenominazione(dto.getDenominazione());
        entity.setDescrizione(dto.getDescrizione());
        entity.setCodiceDitta(dto.getCodiceDitta());
        entity.setRagioneSociale(dto.getRagioneSociale());
        entity.setStatoAmministrativo(dto.getStatoAmministrativo());
        entity.setTipoProcedura(dto.getTipoProcedura());
        entity.setForma(dto.getForma());
        entity.setCodiceAtc(dto.getCodiceAtc());
        entity.setPaAssociati(dto.getPaAssociati());
        entity.setFornitura(dto.getFornitura());
        entity.setLinkFi(dto.getLinkFi());
        entity.setLinkRcp(dto.getLinkRcp());
    }

    public MedicineDto toDto(MedicineEntity entity) {
        MedicineDto dto = new MedicineDto();
        dto.setId(entity.getId());
        dto.setCodiceAic(entity.getCodiceAic());
        dto.setCodFarmaco(entity.getCodFarmaco());
        dto.setCodConfezione(entity.getCodConfezione());
        dto.setDenominazione(entity.getDenominazione());
        dto.setDescrizione(entity.getDescrizione());
        dto.setCodiceDitta(entity.getCodiceDitta());
        dto.setRagioneSociale(entity.getRagioneSociale());
        dto.setStatoAmministrativo(entity.getStatoAmministrativo());
        dto.setTipoProcedura(entity.getTipoProcedura());
        dto.setForma(entity.getForma());
        dto.setCodiceAtc(entity.getCodiceAtc());
        dto.setPaAssociati(entity.getPaAssociati());
        dto.setFornitura(entity.getFornitura());
        dto.setLinkFi(entity.getLinkFi());
        dto.setLinkRcp(entity.getLinkRcp());
        return dto;
    }

    public MedicineEntity toEntity(MedicineDto dto) {
        MedicineEntity entity = new MedicineEntity();
        entity.setId(dto.getId());
        updateEntity(entity, dto);
        return entity;
    }
}