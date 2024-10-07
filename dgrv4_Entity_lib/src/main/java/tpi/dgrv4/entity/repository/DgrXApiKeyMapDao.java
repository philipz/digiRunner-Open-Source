package tpi.dgrv4.entity.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import tpi.dgrv4.entity.entity.DgrXApiKeyMap;

@Repository
public interface DgrXApiKeyMapDao extends JpaRepository<DgrXApiKeyMap, Long>, DgrXApiKeyMapSuperDao {

	public List<DgrXApiKeyMap> findByCreateUser(String CreateUser);

	public List<DgrXApiKeyMap> findByRefApiKeyId(Long refApiKeyId);
	
	public List<DgrXApiKeyMap> deleteByRefApiKeyId(Long refApiKeyId);
	
	public DgrXApiKeyMap findFirstByRefApiKeyIdAndGroupId(Long refApiKeyId, String groupId);
}
