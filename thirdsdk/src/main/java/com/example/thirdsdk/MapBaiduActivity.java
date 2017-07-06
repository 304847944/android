package com.example.thirdsdk;

import java.util.ArrayList;
import java.util.List;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.ArcOptions;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BaiduMap.OnMapClickListener;
import com.baidu.mapapi.map.BaiduMap.OnMarkerClickListener;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.CircleOptions;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.PolygonOptions;
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.map.Stroke;
import com.baidu.mapapi.map.TextOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.overlayutil.PoiOverlay;
import com.baidu.mapapi.search.core.CityInfo;
import com.baidu.mapapi.search.core.PoiInfo;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.poi.OnGetPoiSearchResultListener;
import com.baidu.mapapi.search.poi.PoiCitySearchOption;
import com.baidu.mapapi.search.poi.PoiDetailResult;
import com.baidu.mapapi.search.poi.PoiDetailSearchOption;
import com.baidu.mapapi.search.poi.PoiIndoorResult;
import com.baidu.mapapi.search.poi.PoiNearbySearchOption;
import com.baidu.mapapi.search.poi.PoiResult;
import com.baidu.mapapi.search.poi.PoiSearch;
import com.baidu.mapapi.search.sug.OnGetSuggestionResultListener;
import com.baidu.mapapi.search.sug.SuggestionResult;
import com.baidu.mapapi.search.sug.SuggestionSearch;
import com.baidu.mapapi.search.sug.SuggestionSearchOption;
import com.example.thirdsdk.util.MapBaiduUtil;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by ouyangshen on 2016/12/18.
 */
public class MapBaiduActivity extends AppCompatActivity implements OnClickListener,
		OnMapClickListener, OnGetPoiSearchResultListener,
		OnGetSuggestionResultListener {
	private static final String TAG = "MapBaiduActivity";

	private Spinner sp_poi_method;
	private TextView scope_desc, loc_position;
	private int search_method;
	private String[] searchArray = { "城市中搜索", "在周边搜索" };
	private int SEARCH_CITY = 0;
	private int SEARCH_NEARBY = 1;
	private boolean is_pause = false;

	private void setMethodSpinner(Context context, int spinner_id, int seq) {
		sp_poi_method = (Spinner) findViewById(spinner_id);
		ArrayAdapter<String> county_adapter;
		county_adapter = new ArrayAdapter<String>(context,
				R.layout.item_select, searchArray);
		county_adapter.setDropDownViewResource(R.layout.item_select);
		// setPrompt是设置弹出对话框的标题
		sp_poi_method.setPrompt("请选择POI搜索方式");
		sp_poi_method.setAdapter(county_adapter);
		sp_poi_method
				.setOnItemSelectedListener(new SpinnerSelectedListenerOrder());
		if (seq >= 0) {
			sp_poi_method.setSelection(seq, true);
		} else {
			sp_poi_method.setFocusable(false);
		}
	}

	class SpinnerSelectedListenerOrder implements OnItemSelectedListener {
		public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			search_method = arg2;
			if (search_method == SEARCH_CITY) {
				scope_desc.setText("市内找");
			} else if (search_method == SEARCH_NEARBY) {
				scope_desc.setText("米内找");
			}
			mScope.setText("");
			mKey.setText("");
		}

		public void onNothingSelected(AdapterView<?> arg0) {
		}
	}

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// 注意该方法要在setContentView方法之前实现
		SDKInitializer.initialize(getApplicationContext());
		setContentView(R.layout.activity_map_baidu);
		scope_desc = (TextView) findViewById(R.id.scope_desc);
		loc_position = (TextView) findViewById(R.id.loc_position);
		setMethodSpinner(this, R.id.sp_poi_method, SEARCH_CITY);
		initLocation();
		initMap();
	}

	@Override
	public void onClick(View v) {
		int resid = v.getId();
		if (resid == R.id.search) {
			searchButtonProcess(v);
		} else if (resid == R.id.map_next_data) {
			goToNextPage(v);
		} else if (resid == R.id.map_clear_data) {
			mScope.setText("");
			mKey.setText("");
			// 清除所有图层
			mMapView.getMap().clear();
			posArray.clear();
			is_polygon = false;
		}
		InputMethodManager im = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		im.hideSoftInputFromWindow(getCurrentFocus()
				.getApplicationWindowToken(),
				InputMethodManager.HIDE_NOT_ALWAYS);
	}

	@Override
	public void onPause() {
		mMapView.onPause();
		is_pause = true;
		super.onPause();
	}

	@Override
	public void onResume() {
		if (is_pause == true) {
			mMapView.onResume();
			is_pause = false;
		}
		super.onResume();
	}

	@Override
	public void onDestroy() {
		// 退出时销毁定位
		if (null != mLocClient) {
			mLocClient.stop();
			mLocClient = null;
		}
		// 关闭定位图层
		if (mMapLayer != null) {
			mMapLayer.setMyLocationEnabled(false);
		}
		mMapView.onDestroy();
		mMapView = null;
		mPoiSearch.destroy();
		mSuggestionSearch.destroy();
		super.onDestroy();
	}

	private double m_latitude;
	private double m_longitude;

	// 以下主要是POI搜索用到的代码
	private PoiSearch mPoiSearch = null;
	private SuggestionSearch mSuggestionSearch = null;
	private AutoCompleteTextView mKey = null;
	private EditText mScope = null;
	private Button btn_search, btn_nextpage, btn_cleardata;
	private ArrayAdapter<String> sugAdapter = null;
	private int load_Index = 0;

	private void initMap() {
		mPoiSearch = PoiSearch.newInstance();
		mPoiSearch.setOnGetPoiSearchResultListener(this);
		mSuggestionSearch = SuggestionSearch.newInstance();
		mSuggestionSearch.setOnGetSuggestionResultListener(this);
		mScope = (EditText) findViewById(R.id.poi_city);
		mKey = (AutoCompleteTextView) findViewById(R.id.poi_searchkey);
		btn_search = (Button) findViewById(R.id.search);
		btn_nextpage = (Button) findViewById(R.id.map_next_data);
		btn_cleardata = (Button) findViewById(R.id.map_clear_data);
		btn_search.setOnClickListener(this);
		btn_nextpage.setOnClickListener(this);
		btn_cleardata.setOnClickListener(this);
		sugAdapter = new ArrayAdapter<String>(this,
				R.layout.item_select);
		mKey.setAdapter(sugAdapter);

		// 当输入关键字变化时，动态更新建议列表
		mKey.addTextChangedListener(new TextWatcher() {

			@Override
			public void afterTextChanged(Editable arg0) {
			}

			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1,
					int arg2, int arg3) {
			}

			@Override
			public void onTextChanged(CharSequence cs, int arg1, int arg2,
					int arg3) {
				if (cs.length() <= 0) {
					return;
				}
				String city = mScope.getText().toString();
				// 使用建议搜索服务获取建议列表，结果在onGetSuggestionResult中更新
				mSuggestionSearch
						.requestSuggestion((new SuggestionSearchOption())
								.keyword(cs.toString()).city(city));
			}
		});
	}

	@Override
	public void onGetSuggestionResult(SuggestionResult res) {
		if (res == null || res.getAllSuggestions() == null) {
			return;
		} else {
			sugAdapter.clear();
			for (SuggestionResult.SuggestionInfo info : res.getAllSuggestions()) {
				if (info.key != null) {
					sugAdapter.add(info.key);
				}
			}
			sugAdapter.notifyDataSetChanged();
		}
	}

	// 影响搜索按钮点击事件
	public void searchButtonProcess(View v) {
		Log.d(TAG, "editCity=" + mScope.getText().toString()
				+ ", editSearchKey=" + mKey.getText().toString()
				+ ", load_Index=" + load_Index);
		String keyword = mKey.getText().toString();
		if (search_method == SEARCH_CITY) {
			String city = mScope.getText().toString();
			mPoiSearch.searchInCity((new PoiCitySearchOption()).city(city)
					.keyword(keyword).pageNum(load_Index));
		} else if (search_method == SEARCH_NEARBY) {
			LatLng position = new LatLng(m_latitude, m_longitude);
			int radius = Integer.parseInt(mScope.getText().toString());
			mPoiSearch.searchNearby((new PoiNearbySearchOption())
					.location(position).keyword(keyword).radius(radius)
					.pageNum(load_Index));
		}
	}

	public void goToNextPage(View v) {
		load_Index++;
		searchButtonProcess(null);
	}

	public void onGetPoiResult(PoiResult result) {
		if (result == null
				|| result.error == SearchResult.ERRORNO.RESULT_NOT_FOUND) {
			Toast.makeText(this, "未找到结果", Toast.LENGTH_LONG).show();
			return;
		} else if (result.error == SearchResult.ERRORNO.NO_ERROR) {
			mMapLayer.clear();
			PoiOverlay overlay = new MyPoiOverlay(mMapLayer);
			mMapLayer.setOnMarkerClickListener(overlay);
			List<PoiInfo> poiList = result.getAllPoi();
			overlay.setData(result);
			overlay.addToMap();
			overlay.zoomToSpan();
			// for (PoiInfo poi : poiList) {
			// String detail = String.format(
			// "uid=%s,city=%s,name=%s,phone=%s, address=%s", poi.uid,
			// poi.city, poi.name, poi.phoneNum, poi.address);
			// Log.d(TAG, detail); // 坐标为poi.location（LatLng结构）
			// }
		} else if (result.error == SearchResult.ERRORNO.AMBIGUOUS_KEYWORD) {
			// 当输入关键字在本市没有找到，但在其他城市找到时，返回包含该关键字信息的城市列表
			String strInfo = "在";
			for (CityInfo cityInfo : result.getSuggestCityList()) {
				strInfo += cityInfo.city + ",";
			}
			strInfo += "找到结果";
			Toast.makeText(this, strInfo, Toast.LENGTH_LONG).show();
		}
	}

	public void onGetPoiDetailResult(PoiDetailResult result) {
		if (result.error != SearchResult.ERRORNO.NO_ERROR) {
			Toast.makeText(this, "抱歉，未找到结果", Toast.LENGTH_SHORT).show();
		} else {
			Log.d(TAG,
					"name=" + result.getName() + ",address="
							+ result.getAddress() + ",detail_url="
							+ result.getDetailUrl() + ",shop_hours="
							+ result.getShopHours() + ",telephone="
							+ result.getTelephone() + ",price="
							+ result.getPrice() + ",type=" + result.getType()
							+ ",tag=" + result.getTag());
			Toast.makeText(this, result.getName() + ": " + result.getAddress(),
					Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	public void onGetPoiIndoorResult(PoiIndoorResult result) {
	}

	private class MyPoiOverlay extends PoiOverlay {

		public MyPoiOverlay(BaiduMap baiduMap) {
			super(baiduMap);
		}

		@Override
		public boolean onPoiClick(int index) {
			super.onPoiClick(index);
			PoiInfo poi = getPoiResult().getAllPoi().get(index);
			mPoiSearch.searchPoiDetail((new PoiDetailSearchOption())
					.poiUid(poi.uid));
			return true;
		}
	}

	// 以下主要是定位用到的代码
	private MapView mMapView;
	private BaiduMap mMapLayer;
	private LocationClient mLocClient;
	private boolean isFirstLoc = true;// 是否首次定位

	private void initLocation() {
		mMapView = (MapView) findViewById(R.id.bmapView);
		// 先隐藏地图，待定位到当前城市时再显示
		mMapView.setVisibility(View.INVISIBLE);
		mMapLayer = mMapView.getMap();
		mMapLayer.setOnMapClickListener(this);
		// 开启定位图层
		mMapLayer.setMyLocationEnabled(true);
		mLocClient = new LocationClient(this);
		// 设置定位监听器
		mLocClient.registerLocationListener(new MyLocationListenner());
		LocationClientOption option = new LocationClientOption();
		option.setOpenGps(true);// 打开gps
		option.setCoorType("bd09ll"); // 设置坐标类型
		option.setScanSpan(1000);
		option.setIsNeedAddress(true); // 设置true才能获得详细的地址信息
		// 设置定位参数
		mLocClient.setLocOption(option);
		// 开始定位
		mLocClient.start();
		// 获取最近一次的位置
		// mLocClient.getLastKnownLocation();
	}

	public class MyLocationListenner implements BDLocationListener {

		@Override
		public void onReceiveLocation(BDLocation location) {
			// map view 销毁后不再处理新接收的位置
			if (location == null || mMapView == null) {
				Log.d(TAG, "location is null or mMapView is null");
				return;
			}
			m_latitude = location.getLatitude();
			m_longitude = location.getLongitude();
			String position = String.format("当前位置：%s|%s|%s|%s|%s|%s|%s",
					location.getProvince(), location.getCity(),
					location.getDistrict(), location.getStreet(),
					location.getStreetNumber(), location.getAddrStr(),
					location.getTime());
			loc_position.setText(position);
			MyLocationData locData = new MyLocationData.Builder()
					.accuracy(location.getRadius())
					// 此处设置开发者获取到的方向信息，顺时针0-360
					.direction(100).latitude(m_latitude).longitude(m_longitude)
					.build();
			mMapLayer.setMyLocationData(locData);
//			Toast.makeText(MapBaiduActivity.this, "isFirstLoc=" + isFirstLoc,
//					Toast.LENGTH_LONG).show();
			if (isFirstLoc) {
				isFirstLoc = false;
				LatLng ll = new LatLng(m_latitude, m_longitude);
				MapStatusUpdate update = MapStatusUpdateFactory.newLatLngZoom(ll, 14);
				mMapLayer.animateMapStatus(update);
				// MapStatus oldStatus = mMapLayer.getMapStatus();
				// MapStatus newStatus = new
				// MapStatus.Builder(oldStatus).target(ll).zoom(14).build();
				// MapStatusUpdate z =
				// MapStatusUpdateFactory.newMapStatus(newStatus);
				// mMapLayer.setMapStatus(z);
				// 定位到当前城市时再显示图层
				mMapView.setVisibility(View.VISIBLE);
			}
		}

		public void onReceivePoi(BDLocation poiLocation) {
		}
	}

	// 下面是在地图上添加绘图操作
	private static int lineColor = 0x55FF0000;
	private static int arcColor = 0xbb00FFFF;
	private static int textColor = 0x990000FF;
	private static int polygonColor = 0x77FFFF00;
	private static int radius = 100;
	private ArrayList<LatLng> posArray = new ArrayList<LatLng>();
	boolean is_polygon = false;

	private void addDot(LatLng pos) {
		if (is_polygon == true && posArray.size() > 1
				&& MapBaiduUtil.isInsidePolygon(pos, posArray) == true) {
			Log.d(TAG, "isInsidePolygon");
			LatLng centerPos = MapBaiduUtil.getCenterPos(posArray);
			OverlayOptions ooText = new TextOptions().bgColor(0x00ffffff)
					.fontSize(26).fontColor(textColor).text("标题")// .rotate(-30)
					.position(centerPos);
			mMapLayer.addOverlay(ooText);
			return;
		}
		if (is_polygon == true) {
			Log.d(TAG, "is_polygon == true");
			posArray.clear();
			is_polygon = false;
		}
		boolean is_first = false;
		LatLng thisPos = pos;
		if (posArray.size() > 0) {
			LatLng firstPos = posArray.get(0);
			int distance = (int) Math.round(MapBaiduUtil.getShortDistance(
					thisPos.longitude, thisPos.latitude, firstPos.longitude,
					firstPos.latitude));
			// 多次点击起点，要忽略之
			if (posArray.size() == 1 && distance <= 0) {
				return;
			} else if (posArray.size() > 1) {
				LatLng lastPos = posArray.get(posArray.size() - 1);
				int lastDistance = (int) Math.round(MapBaiduUtil.getShortDistance(
						thisPos.longitude, thisPos.latitude, lastPos.longitude,
						lastPos.latitude));
				// 重复响应当前位置的点击，要忽略之
				if (lastDistance <= 0) {
					return;
				}
			}
			if (distance < radius * 2) {
				thisPos = firstPos;
				is_first = true;
			}
			Log.d(TAG, "distance=" + distance + ", radius=" + radius
					+ ", is_first=" + is_first);

			// 画直线
			LatLng lastPos = posArray.get(posArray.size() - 1);
			List<LatLng> points = new ArrayList<LatLng>();
			points.add(lastPos);
			points.add(thisPos);
			OverlayOptions ooPolyline = new PolylineOptions().width(2)
					.color(lineColor).points(points);
			mMapLayer.addOverlay(ooPolyline);

			// 下面计算两点之间距离
			distance = (int) Math.round(MapBaiduUtil.getShortDistance(
					thisPos.longitude, thisPos.latitude, lastPos.longitude,
					lastPos.latitude));
			String disText = "";
			if (distance > 1000) {
				disText = Math.round(distance * 10 / 1000) / 10d + "公里";
			} else {
				disText = distance + "米";
			}
			LatLng llText = new LatLng(
					(thisPos.latitude + lastPos.latitude) / 2,
					(thisPos.longitude + lastPos.longitude) / 2);
			OverlayOptions ooText = new TextOptions().bgColor(0x00ffffff)
					.fontSize(24).fontColor(textColor).text(disText)// .rotate(-30)
					.position(llText);
			mMapLayer.addOverlay(ooText);
		}
		if (is_first != true) {
//			// 画圆圈
//			OverlayOptions ooCircle = new CircleOptions().fillColor(lineColor)
//					.center(thisPos).stroke(new Stroke(2, 0xAAFF0000))
//					.radius(radius);
//			mMapLayer.addOverlay(ooCircle);
			// 画图片标记
			BitmapDescriptor bitmapDesc = BitmapDescriptorFactory
					.fromResource(R.drawable.icon_geo);
			OverlayOptions ooMarker = new MarkerOptions().draggable(false)
					.visible(true).icon(bitmapDesc).position(thisPos);
			mMapLayer.addOverlay(ooMarker);
			mMapLayer.setOnMarkerClickListener(new OnMarkerClickListener() {
				@Override
				public boolean onMarkerClick(Marker marker) {
					LatLng markPos = marker.getPosition();
					addDot(markPos);
					return true;
				}
			});
		} else {
			Log.d(TAG, "posArray.size()=" + posArray.size());
			// 可能存在地图与标记同时响应点击事件的情况
			if (posArray.size() < 3) {
				posArray.clear();
				is_polygon = false;
				return;
			}
			// 画多边形
			OverlayOptions ooPolygon = new PolygonOptions().points(posArray)
					.stroke(new Stroke(1, 0xFF00FF00)).fillColor(polygonColor);
			mMapLayer.addOverlay(ooPolygon);
			is_polygon = true;

			// 下面计算多边形的面积
			LatLng centerPos = MapBaiduUtil.getCenterPos(posArray);
			double area = Math.round(MapBaiduUtil.getArea(posArray));
			String areaText = "";
			if (area > 1000000) {
				areaText = Math.round(area * 100 / 1000000) / 100d + "平方公里";
			} else {
				areaText = (int) area + "平方米";
			}
			OverlayOptions ooText = new TextOptions().bgColor(0x00ffffff)
					.fontSize(26).fontColor(textColor).text(areaText)// .rotate(-30)
					.position(centerPos);
			mMapLayer.addOverlay(ooText);
		}
		posArray.add(thisPos);
		if (posArray.size() >= 3) {
			// 画弧线
			OverlayOptions ooArc = new ArcOptions()
					.color(arcColor)
					.width(2)
					.points(posArray.get(posArray.size() - 1),
							posArray.get(posArray.size() - 2),
							posArray.get(posArray.size() - 3));
			mMapLayer.addOverlay(ooArc);
		}
	}

	@Override
	public void onMapClick(LatLng arg0) {
		addDot(arg0);
	}

	@Override
	public boolean onMapPoiClick(MapPoi arg0) {
		addDot(arg0.getPosition());
		return false;
	}

}
