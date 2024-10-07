package tpi.dgrv4.entity.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import tpi.dgrv4.entity.entity.DpFile;



@Repository
public interface DpFileDao extends JpaRepository<DpFile, Long>, DpFileSuperDao {
	
	List<DpFile> findByModuleNameAndApiKey(String moduleName, String apiKey);
}
