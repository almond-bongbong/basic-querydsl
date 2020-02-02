package study.querydsl.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;
import study.querydsl.dto.MemberSearchCondition;
import study.querydsl.entity.Member;
import study.querydsl.repository.support.Querydsl4RepositorySupport;

import java.util.List;

import static org.springframework.util.StringUtils.hasText;
import static study.querydsl.entity.QMember.member;
import static study.querydsl.entity.QTeam.team;

@Repository
public class MemberTestRepository extends Querydsl4RepositorySupport {

	public MemberTestRepository() {
		super(Member.class);
	}

	public List<Member> basicSelect() {
		return select(member)
				.from(member)
				.fetch();
	}

	public List<Member> basicSelectFrom() {
		return selectFrom(member)
				.fetch();
	}

	// 기존 버전 살짝 튜닝
	public Page<Member> searchPageByApplyPage(MemberSearchCondition condition, Pageable pageable) {
		JPAQuery<Member> query = selectFrom(member)
				.leftJoin(member.team, team)
				.where(
						usernameEq(condition.getUsername()),
						teamNameEq(condition.getTeamName()),
						ageGoe(condition.getAgeGoe()),
						ageLoe(condition.getAgeLoe())
				);

		List<Member> content = getQuerydsl().applyPagination(pageable, query)
				.fetch();
		return PageableExecutionUtils.getPage(content, pageable, query::fetchCount);
	}

	// 커스텀 util 메소드 이용
	public Page<Member> applyPagination(MemberSearchCondition condition, Pageable pageable) {
		return applyPagination(pageable, query -> query
				.selectFrom(member)
				.leftJoin(member.team, team)
				.where(
						usernameEq(condition.getUsername()),
						teamNameEq(condition.getTeamName()),
						ageGoe(condition.getAgeGoe()),
						ageLoe(condition.getAgeLoe())
				)
		);
	}

	// 커스텀 util 메소드 이용 (complex 버전)
	public Page<Member> applyPagination2(MemberSearchCondition condition, Pageable pageable) {
		return applyPagination(pageable, contentQuery -> contentQuery
				.selectFrom(member)
				.leftJoin(member.team, team)
				.where(
						usernameEq(condition.getUsername()),
						teamNameEq(condition.getTeamName()),
						ageGoe(condition.getAgeGoe()),
						ageLoe(condition.getAgeLoe())
				),
				countQuery -> countQuery
				.selectFrom(member)
				.where(
						usernameEq(condition.getUsername()),
						teamNameEq(condition.getTeamName()),
						ageGoe(condition.getAgeGoe()),
						ageLoe(condition.getAgeLoe())
				)
		);
	}

	private BooleanExpression ageBetween(Integer ageLoe, Integer ageGoe) {
		return ageLoe != null && ageGoe != null ? ageLoe(ageLoe).and(ageGoe(ageGoe)) : null;
	}

	private BooleanExpression usernameEq(String username) {
		return hasText(username) ? member.username.eq(username) : null;
	}

	private BooleanExpression teamNameEq(String teamName) {
		return hasText(teamName) ? team.name.eq(teamName) : null;
	}

	private BooleanExpression ageGoe(Integer ageGoe) {
		return ageGoe != null ? member.age.goe(ageGoe) : null;
	}

	private BooleanExpression ageLoe(Integer ageLoe) {
		return ageLoe != null ? member.age.loe(ageLoe) : null;
	}
}
