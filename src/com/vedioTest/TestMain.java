package com.vedioTest;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.aliyun.api.AliyunClient;
import com.aliyun.api.DefaultAliyunClient;
import com.aliyun.api.domain.AnalysisJob;
import com.aliyun.api.domain.Job;
import com.aliyun.api.domain.OSSFile;
import com.aliyun.api.domain.Template;
import com.aliyun.api.mts.mts20140618.request.QueryAnalysisJobListRequest;
import com.aliyun.api.mts.mts20140618.request.QueryJobListRequest;
import com.aliyun.api.mts.mts20140618.request.SearchPipelineRequest;
import com.aliyun.api.mts.mts20140618.request.SubmitAnalysisJobRequest;
import com.aliyun.api.mts.mts20140618.request.SubmitJobsRequest;
import com.aliyun.api.mts.mts20140618.response.QueryAnalysisJobListResponse;
import com.aliyun.api.mts.mts20140618.response.QueryJobListResponse;
import com.aliyun.api.mts.mts20140618.response.SearchPipelineResponse;
import com.aliyun.api.mts.mts20140618.response.SubmitAnalysisJobResponse;
import com.aliyun.api.mts.mts20140618.response.SubmitJobsResponse;
import com.taobao.api.ApiException;

public class TestMain {

	public static String MTS_SERVER_API_URL="http://mts.aliyuncs.com/";
	public static final String location="oss-cn-hangzhou";//目前只支持杭州
	public static String access_key_id="xxx";
	public static String access_key_secret="xxx";
	public static AliyunClient aliyunClient;
	public static String bucket="xxx";
	public static String templateId="S00000001-200010";//转码模板（使用的是阿里云自带的静态转码模板）
	public static String waterMarkTemplateId="xx";//水印模板id（自己在账户上添加模板）.
	public static void main(String[] args) {
		aliyunClient = new DefaultAliyunClient(MTS_SERVER_API_URL, access_key_id, access_key_secret);
		OSSMtsVO inputFile=new OSSMtsVO(bucket, location, "xxx/xxx.FLV");//xxx.FLV 已经在oss上存储的视频
		OSSMtsVO waterMarkFile = new OSSMtsVO(bucket, location, "xxx/xxx.png");//注意：水印图片必须是png格式
		String PipelineId = SearchPipelineId();//注：1.目前一个账号只有一个管道id,2.可以直接在阿里云账号内查看固定的管道id，此步骤可省略
		//System.out.println("PipelineId:"+SearchPipelineId());
		String jobId = SubmitJob(inputFile,waterMarkFile, templateId,PipelineId);
		Job job = QueryJobList(jobId);
		OSSFile outputFile = job.getOutput().getOutputFile();
		String outputFileOSSUrl = "http://" + outputFile.getBucket() + ".oss-cn-hangzhou.aliyuncs.com/" + outputFile.getObject();
		System.out.println("success! ossFile url :"+outputFileOSSUrl);
	}
	
	/**
	 * 查找管道id
	 * 注：由于目前一个账号只有一个管道id,所以可以直接在阿里云账号内查看管道id，此步骤可省略。
	 * @return
	 */
	public static String SearchPipelineId(){
		SearchPipelineRequest request = new SearchPipelineRequest();
		try {
			SearchPipelineResponse response = aliyunClient.execute(request);
			if(!response.isSuccess()){
				throw new RuntimeException("SearchPipelineRequest failed!");
			}
			return response.getPipelineList().get(0).getId();
		} catch (ApiException e) {
			throw new RuntimeException(e);
		}
		
	}
	
	/**
	 * 提交转码作业
	 * @param inputFile
	 */
	public static String SubmitJob(OSSMtsVO inputFile,OSSMtsVO waterMarkFile,String templateId,String pipelineId){
		SubmitJobsRequest request = new SubmitJobsRequest();
		System.out.println("inputFIle:"+inputFile.toJson().toJSONString());
		
		JSONObject waterMarkConfig = new JSONObject();
		waterMarkConfig.put("InputFile", waterMarkFile.toJson());
		waterMarkConfig.put("WaterMarkTemplateId", waterMarkTemplateId);
		JSONArray waterMarkConfigArray = new JSONArray();
		waterMarkConfigArray.add(waterMarkConfig);
		
		request.setInput(inputFile.toJson().toJSONString());
		request.setOutputBucket(bucket);
		request.setPipelineId(pipelineId);
		
		JSONObject output = new JSONObject();
		output.put("OutputObject","123.mp4" );
		output.put("TemplateId", templateId);
		output.put("WaterMarks", waterMarkConfigArray);
		JSONArray outputs = new JSONArray();
		outputs.add(output);
		request.setOutputs(outputs.toJSONString());
		System.out.println("outputs.toJsonString :"+outputs.toJSONString());
		try {
			SubmitJobsResponse response = aliyunClient.execute(request);
			if(!response.isSuccess()){
				throw new RuntimeException("SubmitJobsRequest failed");
			}
			return response.getJobResultList().get(0).getJob().getJobId();
		} catch (ApiException e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	*查找是否成功
	*/
	public static Job QueryJobList(String jobId){
		QueryJobListRequest request = new QueryJobListRequest();
		request.setJobIds(jobId);
		try {
			while(true){
			QueryJobListResponse response = aliyunClient.execute(request);
			if(!response.isSuccess()){
				throw new RuntimeException("QueryJobListRequest failed!");
			}
			Job job = response.getJobList().get(0);
			String state = job.getState();
			System.out.println("job.State : "+state);
			if("TranscodeSuccess".equals(state)){
				return job;
			}
			if("TranscodeFail".equals(state)){
				throw new RuntimeException("QueryJobListRequest state failed");
			}
			Thread.sleep(5*1000);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return null;
		
	}

	
}
