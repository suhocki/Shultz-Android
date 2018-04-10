package suhockii.dev.shultz.util;

import android.support.v7.util.DiffUtil;

import java.util.List;

import suhockii.dev.shultz.entity.BaseEntity;

public class ShultzDiffUtilCallback extends DiffUtil.Callback {

    private final List<BaseEntity> oldList;
    private final List<BaseEntity> newList;

    public ShultzDiffUtilCallback(List<BaseEntity> oldList, List<BaseEntity> newList) {
        this.oldList = oldList;
        this.newList = newList;
    }

    @Override
    public int getOldListSize() {
        return oldList.size();
    }

    @Override
    public int getNewListSize() {
        return newList.size();
    }

    @Override
    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
        BaseEntity oldShultzInfoEntity = oldList.get(oldItemPosition);
        BaseEntity newShultzInfoEntity = newList.get(newItemPosition);
        return oldShultzInfoEntity.getId().equals(newShultzInfoEntity.getId());
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        BaseEntity oldShultzInfoEntity = oldList.get(oldItemPosition);
        BaseEntity newShultzInfoEntity = newList.get(newItemPosition);
        return oldShultzInfoEntity.getId().equals(newShultzInfoEntity.getId());
    }
}
