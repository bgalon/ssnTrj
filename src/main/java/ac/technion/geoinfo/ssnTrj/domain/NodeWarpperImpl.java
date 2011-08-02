package ac.technion.geoinfo.ssnTrj.domain;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.ReturnableEvaluator;
import org.neo4j.graphdb.StopEvaluator;
import org.neo4j.graphdb.Traverser;
import org.neo4j.graphdb.Traverser.Order;

public class NodeWarpperImpl implements NodeWrapper,Static {

	final Node underlayingNode;
	
	public NodeWarpperImpl(Node theUnderlaying)
	{
		underlayingNode = theUnderlaying;
	}
	
	public Node getNode()
	{
		return underlayingNode;
	}
	
	public String getType()
	{
		if (underlayingNode.hasProperty(SSN_TYPE))
			return (String)underlayingNode.getProperty(SSN_TYPE);
		return null;
	}
	
	//Node implementation
	//************************
	public long getId() {
		return underlayingNode.getId();
	}

	public void delete() {
		underlayingNode.delete();
	}

	public Iterable<Relationship> getRelationships() {
		return underlayingNode.getRelationships();
	}

	public boolean hasRelationship() {
		return underlayingNode.hasRelationship();
	}

	public Iterable<Relationship> getRelationships(RelationshipType... types) {
		return underlayingNode.getRelationships(types);
	}

	public Iterable<Relationship> getRelationships(Direction direction,
			RelationshipType... types) {
		return underlayingNode.getRelationships(direction, types);
	}

	public boolean hasRelationship(RelationshipType... types) {
		return underlayingNode.hasRelationship(types);
	}

	public boolean hasRelationship(Direction direction,
			RelationshipType... types) {
		return underlayingNode.hasRelationship(direction, types);
	}

	public Iterable<Relationship> getRelationships(Direction dir) {
		return underlayingNode.getRelationships(dir);
	}

	public boolean hasRelationship(Direction dir) {
		return underlayingNode.hasRelationship(dir);
	}

	public Iterable<Relationship> getRelationships(RelationshipType type,
			Direction dir) {
		return underlayingNode.getRelationships(type, dir);
	}

	public boolean hasRelationship(RelationshipType type, Direction dir) {
		return underlayingNode.hasRelationship(type, dir);
	}

	public Relationship getSingleRelationship(RelationshipType type,
			Direction dir) {
		return underlayingNode.getSingleRelationship(type, dir);
	}

	public Relationship createRelationshipTo(Node otherNode,
			RelationshipType type) {
		return underlayingNode.createRelationshipTo(otherNode, type);
	}

	public Traverser traverse(Order traversalOrder,
			StopEvaluator stopEvaluator,
			ReturnableEvaluator returnableEvaluator,
			RelationshipType relationshipType, Direction direction) {
		return underlayingNode.traverse(traversalOrder, stopEvaluator, returnableEvaluator,
				relationshipType, direction);
	}

	public Traverser traverse(Order traversalOrder,
			StopEvaluator stopEvaluator,
			ReturnableEvaluator returnableEvaluator,
			RelationshipType firstRelationshipType, Direction firstDirection,
			RelationshipType secondRelationshipType, Direction secondDirection) {
		return underlayingNode.traverse(traversalOrder, stopEvaluator, returnableEvaluator,
				firstRelationshipType, firstDirection, secondRelationshipType, secondDirection);
	}

	public Traverser traverse(Order traversalOrder,
			StopEvaluator stopEvaluator,
			ReturnableEvaluator returnableEvaluator,
			Object... relationshipTypesAndDirections) {
		return underlayingNode.traverse(traversalOrder, stopEvaluator,
				returnableEvaluator, relationshipTypesAndDirections);
	}

	public GraphDatabaseService getGraphDatabase() {
		return underlayingNode.getGraphDatabase();
	}

	public boolean hasProperty(String key) {
		return underlayingNode.hasProperty(key);
	}

	public Object getProperty(String key) {
		return underlayingNode.getProperty(key);
	}

	public Object getProperty(String key, Object defaultValue) {
		return underlayingNode.getProperty(key, defaultValue);
	}

	public void setProperty(String key, Object value) {
		underlayingNode.setProperty(key, value);
	}

	public Object removeProperty(String key) {
		return underlayingNode.removeProperty(key);
	}

	public Iterable<String> getPropertyKeys() {
		return underlayingNode.getPropertyKeys();
	}

	@SuppressWarnings("deprecation")
	public Iterable<Object> getPropertyValues() {
		return underlayingNode.getPropertyValues();
	}

	public double expected() {
		return 0;
	}

	public double getExpected() {
		return 0;
	}
	
	@Override
	public String toString()
	{
		return getType() + "[" + underlayingNode.getId()+ "]";
	}

}
