
package com.creditease.request;

import org.springframework.http.HttpHeaders;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * 
* @Description: 文件上传requestWapper
 */
public class YXDefaultMultipartHttpServletRequest extends XssMultipartHttpServletRequest{
	
	private static final String CONTENT_TYPE = "Content-Type";

	private Map<String, String[]> multipartParameters;

	private Map<String, String> multipartParameterContentTypes;
		
	/**
	 * Wrap the given HttpServletRequest in a MultipartHttpServletRequest.
	 * @param request the servlet request to wrap
	 * @param mpFiles a map of the multipart files
	 * @param mpParams a map of the parameters to expose,
	 * with Strings as keys and String arrays as values
	 */
	public YXDefaultMultipartHttpServletRequest(HttpServletRequest request, MultiValueMap<String, MultipartFile> mpFiles,
                                                Map<String, String[]> mpParams, Map<String, String> mpParamContentTypes, String badSql) {
		super(request);
		setMultipartFiles(mpFiles);
		setMultipartParameters(mpParams);
		setMultipartParameterContentTypes(mpParamContentTypes);
		setBadSqlStr(badSql);
	}

	/**
	 * Wrap the given HttpServletRequest in a MultipartHttpServletRequest.
	 * @param request the servlet request to wrap
	 */
	public YXDefaultMultipartHttpServletRequest(HttpServletRequest request) {
		super(request);
	}
	@Override
	public String getParameterWithXss(String name) {
		String[] values = getMultipartParameters().get(name);
		if (values != null) {
			return (values.length > 0 ? values[0] : null);
		}
		return super.getParameter(name);
	}

	@Override
	public String[] getParameterValuesWithXss(String name) {
		String[] values = getMultipartParameters().get(name);
		if (values != null) {
			return values;
		}
		return super.getParameterValues(name);
	}
	
	@Override
	public Enumeration<String> getParameterNames() {
		Map<String, String[]> multipartParameters = getMultipartParameters();
		if (multipartParameters.isEmpty()) {
			return super.getParameterNames();
		}

		Set<String> paramNames = new LinkedHashSet<String>();
		Enumeration<String> paramEnum = super.getParameterNames();
		while (paramEnum.hasMoreElements()) {
			paramNames.add(paramEnum.nextElement());
		}
		paramNames.addAll(multipartParameters.keySet());
		return Collections.enumeration(paramNames);
	}
	@Override
	public Map<String, String[]> getParameterMap() {
		Map<String, String[]> multipartParameters = getMultipartParameters();
		if (multipartParameters.isEmpty()) {
			return super.getParameterMap();
		}

		Map<String, String[]> paramMap = new LinkedHashMap<String, String[]>();
		paramMap.putAll(super.getParameterMap());
		paramMap.putAll(multipartParameters);
		return paramMap;
	}

	public String getMultipartContentType(String paramOrFileName) {
		MultipartFile file = getFile(paramOrFileName);
		if (file != null) {
			return file.getContentType();
		}
		else {
			return getMultipartParameterContentTypes().get(paramOrFileName);
		}
	}

	public HttpHeaders getMultipartHeaders(String paramOrFileName) {
		String contentType = getMultipartContentType(paramOrFileName);
		if (contentType != null) {
			HttpHeaders headers = new HttpHeaders();
			headers.add(CONTENT_TYPE, contentType);
			return headers;
		}
		else {
			return null;
		}
	}


	/**
	 * Set a Map with parameter names as keys and String array objects as values.
	 * To be invoked by subclasses on initialization.
	 */
	protected final void setMultipartParameters(Map<String, String[]> multipartParameters) {
		this.multipartParameters = multipartParameters;
	}

	/**
	 * Obtain the multipart parameter Map for retrieval,
	 * lazily initializing it if necessary.
	 * @see #initializeMultipart()
	 */
	protected Map<String, String[]> getMultipartParameters() {
		if (this.multipartParameters == null) {
			initializeMultipart();
		}
		return this.multipartParameters;
	}

	/**
	 * Set a Map with parameter names as keys and content type Strings as values.
	 * To be invoked by subclasses on initialization.
	 */
	protected final void setMultipartParameterContentTypes(Map<String, String> multipartParameterContentTypes) {
		this.multipartParameterContentTypes = multipartParameterContentTypes;
	}

	/**
	 * Obtain the multipart parameter content type Map for retrieval,
	 * lazily initializing it if necessary.
	 * @see #initializeMultipart()
	 */
	protected Map<String, String> getMultipartParameterContentTypes() {
		if (this.multipartParameterContentTypes == null) {
			initializeMultipart();
		}
		return this.multipartParameterContentTypes;
	}
}
