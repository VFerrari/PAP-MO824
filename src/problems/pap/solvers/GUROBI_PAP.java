package problems.pap.solvers;

import gurobi.GRB;
import gurobi.GRBEnv;
import gurobi.GRBException;
import gurobi.GRBLinExpr;
import gurobi.GRBModel;
import gurobi.GRBVar;

import java.io.FileWriter;
import java.io.IOException;

import problems.pap.PAP;

public class GUROBI_PAP {
    public static GRBEnv env;
    public static GRBModel model;
    public GRBVar[][] x, y, z;
    public PAP problem;

    public GUROBI_PAP(String filename) throws IOException {
        this.problem = new PAP(filename);
    }

    protected void populateNewModel(GRBModel model) throws GRBException {
    	int i, j, k;
    	
        // decision variables
        x = new GRBVar[problem.P][problem.D];
        for (i = 0; i < problem.P; i++) {
        	for (j = 0; j < problem.D; j++) {
        		x[i][j] = model.addVar(0, 1, 0.0f, GRB.BINARY, "x[" + i + "]["+ j + "]");
        	}
        }
        
        y = new GRBVar[problem.D][problem.T];
        for (i = 0; i < problem.D; i++) {
        	for (j = 0; j < problem.T; j++) {
        		y[i][j] = model.addVar(0, 1, 0.0f, GRB.BINARY, "y[" + i + "]["+ j + "]");
        	}
        }

        z = new GRBVar[problem.P][problem.T];
        for (i = 0; i < problem.P; i++) {
        	for (j = 0; j < problem.T; j++) {
        		z[i][j] = model.addVar(0, 1, 0.0f, GRB.BINARY, "z[" + i + "]["+ j + "]");
        	}
        }

        model.update();

        // objective function
        GRBLinExpr obj = new GRBLinExpr();

        for (i = 0; i < problem.D; i++) {
            for (j = 0; j < problem.P; j++) {
                obj.addTerm(problem.A[j][i]+100, x[j][i]);
            }
        }

        model.setObjective(obj);
        model.update();

        // Constraints
        GRBLinExpr expr, expr2;
        
        // Professor constraints
        for (i=0; i<problem.D; i++) {
            expr = new GRBLinExpr();
            for(j=0; j<problem.P; j++) {
            	expr.addTerm(1, x[j][i]);
            }
            model.addConstr(expr, GRB.LESS_EQUAL, 1.0, String.valueOf("class_" + i + "_profs"));
        }
        
        // Class slots constraints
        for (i=0; i<problem.D; i++) {
            expr = new GRBLinExpr();
            expr2 = new GRBLinExpr();
            for(j=0; j<problem.T; j++) {
            	expr.addTerm(1, y[i][j]);
            }
            for(j=0; j<problem.P; j++) {
            	expr2.addTerm(problem.h[i],  x[j][i]);
            }
            model.addConstr(expr, GRB.EQUAL, expr2, String.valueOf("class_" + i + "_slots"));
        }
        
        // Available classrooms constraints
        for (i=0; i<problem.T; i++) {
            expr = new GRBLinExpr();
            for(j=0; j<problem.D; j++) {
            	expr.addTerm(1, y[j][i]);
            }
            model.addConstr(expr, GRB.LESS_EQUAL, problem.S, String.valueOf("slot_" + i + "_rooms"));
        }
        
        // Availability constraints
        for(i=0; i<problem.P; i++) {
        	for(j=0; j<problem.T; j++) {
        		model.addConstr(z[i][j], GRB.LESS_EQUAL, problem.r[i][j] ? 1:0, String.valueOf("prof_" + i + "_slot_" + j));
        	}
        }
        
        // Professor workload constraints
        for (i=0; i<problem.P; i++) {
            expr = new GRBLinExpr();
            for(j=0; j<problem.T; j++) {
            	expr.addTerm(1, z[i][j]);
            }
            model.addConstr(expr, GRB.LESS_EQUAL, problem.H, String.valueOf("prof_" + i + "_workload"));
        }
        
        // Variable coupling constraints
        for (i=0; i<problem.P; i++) {
        	for(j=0; j<problem.D; j++) {
        		for(k=0; k<problem.T; k++) {
                    expr = new GRBLinExpr();
        			expr.addTerm(1, x[i][j]);
        			expr.addTerm(1, y[j][k]);
        			expr.addConstant(-1);
        			model.addConstr(expr, GRB.LESS_EQUAL, z[i][k], String.valueOf("prof_" + i + "_class_" + j + "_slot_" + k));
        		}
        	}
        }
        
        model.update();

        // maximization objective function
        model.set(GRB.IntAttr.ModelSense, -1);
    }

    public static void main(String[] args) throws IOException, GRBException {

        // instances
        //String[] instances = {"P50D50S1.pap", "P50D50S3.pap", "P50D50S5.pap", "P70D70S1.pap", "P70D70S3.pap", "P70D70S5.pap", "P70D100S6.pap", "P70D100S8.pap", "P70D100S10.pap", "P100D150S10.pap", "P100D150S15.pap", "P100D150S20.pap"};
    	String[] instances = {"P50D50S1.pap", "P50D50S5.pap", "P70D70S1.pap", "P70D70S5.pap", "P70D100S6.pap", "P70D100S10.pap"};
    	
        // create text file
        FileWriter fileWriter = new FileWriter("results/GUROBI_PAP.txt");

        for (String instance : instances) {
            // read the problem
            GUROBI_PAP gurobi = new GUROBI_PAP("instances/" + instance);

            // create the environment and model
            env = new GRBEnv();
            model = new GRBModel(env);
            model.getEnv().set(GRB.DoubleParam.TimeLimit, 1800.0);

            // generate the model
            gurobi.populateNewModel(model);

            // solve the model
            model.optimize();

            // save the solution in text file
            fileWriter.append(instance + ";" + (model.get(GRB.DoubleAttr.ObjVal)-100*gurobi.problem.D) + ";" + (model.get(GRB.DoubleAttr.ObjBound)-100*gurobi.problem.D) + ";" + model.get(GRB.DoubleAttr.Runtime) + "\n");

            // dispose the environment and model
            model.dispose();
            env.dispose();
        }
        fileWriter.close();
    }
}
