package com.bt.rsqe.customerinventory.driver;

import com.bt.rsqe.customerinventory.ToBeAssets;
import com.bt.rsqe.customerinventory.dto.AssetDTO;
import com.bt.rsqe.customerinventory.parameter.LengthConstrainingProductInstanceId;
import com.bt.rsqe.customerinventory.parameter.LineItemId;
import com.bt.rsqe.customerinventory.parameter.ProductInstanceVersion;
import com.bt.rsqe.customerinventory.parameter.SiteId;
import com.bt.rsqe.web.rest.exception.BadRequestException;
import com.bt.rsqe.web.rest.exception.ResourceNotFoundException;

import javax.ws.rs.core.UriBuilder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class OptionBasedFutureAssetDriverStub extends OptionBasedFutureAssetDriver {
    private SiteId siteId;
    private LineItemId lineItemId;
    private ToBeAssets assets;

    public OptionBasedFutureAssetDriverStub(ToBeAssets assets,
                                            LineItemId lineItemId) {
        this(assets, lineItemId, null);
    }

    public OptionBasedFutureAssetDriverStub(ToBeAssets assets,
                                            LineItemId lineItemId,
                                            SiteId siteId) {
        super(UriBuilder.fromPath("http://localhost:0").build());
        this.lineItemId = lineItemId;
        this.assets = assets;
        this.siteId = siteId;
    }

    @Override
    public void put(AssetDTO asset) {
        if (lineItemId == null) {
            throw new ResourceNotFoundException();
        }
        if (asset == null) {
            throw new BadRequestException();
        }
        if (asset.getId() == null) {
            throw new ResourceNotFoundException();
        }
        if (asset.getParentId() != null) {
            AssetDTO parent = get(new LengthConstrainingProductInstanceId(asset.getParentId()),
                                        new ProductInstanceVersion(asset.getParentVersion()));
            if (parent != null) {
                List<AssetDTO> children = new ArrayList<AssetDTO>(parent.getChildren());
                for (AssetDTO child : children) {
                    if (child.getId().equals(asset.getId())) {
                        asset.removeRelationship(child);
                    }
                }
                parent.addChild(asset);
            }
        }
        assets.put(new ToBeAssets.Key(asset), asset);
    }

    @Override
    public AssetDTO get(LengthConstrainingProductInstanceId assetId, ProductInstanceVersion productInstanceVersion) {
        if (lineItemId == null) {
            throw new ResourceNotFoundException();
        }
        AssetDTO dto = assets.get(new ToBeAssets.Key(assetId, lineItemId));
        if (dto == null) {
            throw new ResourceNotFoundException();
        }
        if (siteId != null && !siteId.toString().equals(dto.getSiteId())) {
            throw new ResourceNotFoundException();
        }
        return dto;
    }

    @Override
    public AssetDTO get() {
        if (lineItemId == null) {
            throw new ResourceNotFoundException();
        }
        AssetDTO dto = null;
        List<AssetDTO> values = new ArrayList<AssetDTO>(assets.values());
        Iterator<AssetDTO> itr = values.iterator();
        while (itr.hasNext() && dto == null) {
            AssetDTO asset = itr.next();
            if (asset.getLineItemId().equals(lineItemId.toString()) &&
                asset.getParentId() == null) {
                dto = asset;
            }
        }
        if (dto == null) {
            throw new ResourceNotFoundException();
        }
        if (siteId != null && !siteId.toString().equals(dto.getSiteId())) {
            throw new ResourceNotFoundException();
        }
        return dto;
    }

    @Override
    public FutureAssetPriceDriver getFutureAssetPriceDriver(LengthConstrainingProductInstanceId assetId) {
        throw new UnsupportedOperationException("Not supported in stub implementation");
    }
}
