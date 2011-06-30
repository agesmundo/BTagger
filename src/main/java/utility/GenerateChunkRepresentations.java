package utility;

public class GenerateChunkRepresentations
{

	/**
	 * Generate the five chunk representations
	 * 
	 * arg[0] source file arg[1] column ID in the source file (starting from 0)
	 */
	public static void main(String arg[])
	{
		String use = "USE:   java GenerateChunkRepresentations <source_file> <column_ID>";
		if (arg.length != 3)
			System.out.println(use);
		else {
			try {
				generate5ChunkRepresentations(arg[0], Integer.parseInt(arg[1]));
			} catch (NumberFormatException e) {
				System.out.println(use);
			}
		}
	}

	public static void generate5ChunkRepresentations(String sourceFile,
			int column)
	{
		ConvertChunkRepresentation.convertChunkRepresentation(sourceFile,
				column, sourceFile + "_IOB1", 1);
		ConvertChunkRepresentation.convertChunkRepresentation(sourceFile,
				column, sourceFile + "_IOB2", 2);
		ConvertChunkRepresentation.convertChunkRepresentation(sourceFile,
				column, sourceFile + "_IOE1", 3);
		ConvertChunkRepresentation.convertChunkRepresentation(sourceFile,
				column, sourceFile + "_IOE2", 4);
		ConvertChunkRepresentation.convertChunkRepresentation(sourceFile,
				column, sourceFile + "_O+C", 5);
	}

	public static void generate4ChunkRepresentations(String sourceFile,
			int column)
	{
		ConvertChunkRepresentation.convertChunkRepresentation(sourceFile,
				column, sourceFile + "_IOB1", 1);
		ConvertChunkRepresentation.convertChunkRepresentation(sourceFile,
				column, sourceFile + "_IOB2", 2);
		ConvertChunkRepresentation.convertChunkRepresentation(sourceFile,
				column, sourceFile + "_IOE1", 3);
		ConvertChunkRepresentation.convertChunkRepresentation(sourceFile,
				column, sourceFile + "_IOE2", 4);
	}
}
